import os
import re
import subprocess
import sys
import argparse
import shutil
from dataclasses import dataclass
from typing import Optional, List

# Constants
KSU_VER_MAJOR = 3
BUILD_TYPES = ['gki', 'avd']
BUILD_MODES = ['gki', 'lkm', 'lkm-gki']
ARCHITECTURES = ['aarch64', 'x86_64']

# Path constants
KERNEL_DRIVERS_REL_PATH = "common/drivers"
KERNEL_KSU_DRIVER_REL_PATH = f"{KERNEL_DRIVERS_REL_PATH}/kernelsu"
KERNEL_DRIVERS_MAKEFILE = f"{KERNEL_DRIVERS_REL_PATH}/Makefile"
KERNEL_DRIVERS_KCONFIG = f"{KERNEL_DRIVERS_REL_PATH}/Kconfig"


@dataclass
class KsuBuildConfig:
    """Configuration for KernelSU build."""
    kernel_source_path: str  # Absolute path to kernel source
    build_arch: str  # Architecture: 'aarch64' or 'x86_64'
    build_type: str  # Build type: 'gki' or 'avd'
    debug: bool  # Enable debug features
    kernel_ver: str  # Kernel version/branch name
    ksu_source_path: str  # Absolute path to KernelSU source
    output_path: str  # Output directory for build artifacts
    build_config_legacy: Optional[str] = None  # Legacy config path


def parse_arguments() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Build Kernel / LKM for KernelSU.")
    parser.add_argument("--arch",
                        type=str,
                        required=True,
                        choices=ARCHITECTURES,
                        help="The architecture to build <aarch64|x86_64>")
    parser.add_argument("--buildtype",
                        type=str,
                        required=True,
                        choices=BUILD_TYPES,
                        help="What type to build <gki|avd>")
    parser.add_argument("--buildmode",
                        type=str,
                        required=True,
                        choices=BUILD_MODES,
                        help="What mode to build <gki|lkm|lkm-gki>")
    parser.add_argument("--build-legacy-cfg",
                        type=str,
                        required=False,
                        help="Kernel config used by make")
    parser.add_argument("--debug",
                        action='store_true',
                        help="Enable debug features for the kernel.")
    parser.add_argument(
        "--github-env-file",
        type=str,
        help="Path to the GitHub environment file to output variables.")
    parser.add_argument("--outpath",
                        type=str,
                        default="KernelSUbuild",
                        help="Build artifacts path, relative to workspace")
    parser.add_argument("--kernel-source-path",
                        type=str,
                        required=True,
                        help="Path to the Android kernel source.")
    parser.add_argument("--ksu-source-path",
                        type=str,
                        required=True,
                        help="Path to the KernelSU source.")
    parser.add_argument("--kernel-version",
                        type=str,
                        required=True,
                        help="Kernel branch name e.g. android12-5.10")
    parser.add_argument("--workspace",
                        type=str,
                        required=True,
                        help="Path to the workspace.")

    args = parser.parse_args()
    return args


def path_to_absolute(base: str, target: str) -> str:
    """
    Convert a relative path to absolute based on base directory.
    
    Args:
        base: Base directory path
        target: Target path (relative or absolute)
    
    Returns:
        Absolute path to target
        
    Raises:
        FileNotFoundError: If base directory does not exist.
    """
    if not os.path.exists(base):
        raise FileNotFoundError(f"Base directory does not exist: {base}")
    if os.path.isabs(target):
        return target
    path = os.path.join(base, target)
    return os.path.abspath(path)


def compare_kernel_versions(kernel_branch_name: str, kver: str) -> int:
    """
    Compare kernel version from branch name with target version.
    
    Returns:
        1 if kernel_branch_name > kver
        0 if equal
        -1 if kernel_branch_name < kver
    """
    pattern = r"(?<=[0-9]-)[0-9]+\.[0-9]+"
    match = re.search(pattern, kernel_branch_name)
    if not match:
        raise ValueError(f"Invalid kernel branch name: {kernel_branch_name}")

    v1 = match.group()
    v1_parts = v1.split('.')
    v2_parts = kver.split('.')

    max_len = max(len(v1_parts), len(v2_parts))

    for i in range(max_len):
        v1_num = int(v1_parts[i]) if i < len(v1_parts) else 0
        v2_num = int(v2_parts[i]) if i < len(v2_parts) else 0
        if v1_num > v2_num:
            return 1
        elif v1_num < v2_num:
            return -1
    return 0


def run_command(command: List[str],
                cwd: Optional[str] = None,
                check: bool = True,
                env: Optional[dict] = None) -> subprocess.CompletedProcess:
    """
    Run a command and stream its output.
    
    Args:
        command: List of command arguments
        cwd: Working directory
        check: If True, raise CalledProcessError on non-zero exit code
        env: Environment variables dictionary
    
    Returns:
        CompletedProcess object with stdout and returncode
    """
    print(f"Executing: {' '.join(command)}", flush=True)
    try:
        process = subprocess.Popen(command,
                                   cwd=cwd,
                                   text=True,
                                   env=env or os.environ.copy(),
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT,
                                   bufsize=1,
                                   universal_newlines=True)

        output_lines = []
        for line in process.stdout:
            sys.stdout.write(line)
            output_lines.append(line)

        process.wait()
        returncode = process.returncode
        full_output = "".join(output_lines)

        if check and returncode != 0:
            raise subprocess.CalledProcessError(returncode,
                                                command,
                                                output=full_output)

        return subprocess.CompletedProcess(command,
                                           returncode,
                                           stdout=full_output,
                                           stderr=None)

    except FileNotFoundError as e:
        print(f"Command not found: {command[0]}")
        raise
    except PermissionError as e:
        print(f"Permission denied when executing: {' '.join(command)}")
        raise
    except subprocess.CalledProcessError as e:
        print(
            f"\nCommand failed with exit code {e.returncode}: {' '.join(command)}"
        )
        if env:
            print(f"running env below:\n{env}")
        sys.exit(e.returncode)
    except Exception as e:
        print(f"Unexpected error executing command: {e}")
        raise


def write_github_env(key: str, value: str) -> None:
    """Write a key-value pair to GitHub environment file."""
    global GITHUB_ENV_FILE_PATH
    if not GITHUB_ENV_FILE_PATH:
        return
    print(f"Writing to GitHub env file: {GITHUB_ENV_FILE_PATH}")
    with open(GITHUB_ENV_FILE_PATH, "a") as f:
        f.write(f"{key}={value}\n")


def calculate_ksu_version(ksu_repo_path: str) -> str:
    """Calculate KernelSU version based on git commit count."""
    result = run_command(["git", "rev-list", "--count", "HEAD"],
                         cwd=ksu_repo_path,
                         check=True)
    commit_count = int(result.stdout.strip())
    kernelsu_version = commit_count + 10000 * KSU_VER_MAJOR
    print(f"KernelSU VERSION: {kernelsu_version}")
    return str(kernelsu_version)


def clean_workspace(android_kernel_path: str) -> None:
    """Clean the repo path and commit changes."""
    print()
    print("=" * 50)
    print("Make working directory clean")
    print("=" * 50)

    run_command(
        ["git", "config", "--global", "user.email", "bot@kernelsu.org"])
    run_command(["git", "config", "--global", "user.name", "KernelSUBot"])
    common_dir = os.path.join(android_kernel_path, "common")
    run_command(["git", "add", "-A"], cwd=common_dir)
    run_command(["git", "commit", "-a", "-m", "Add KernelSU"],
                cwd=common_dir,
                check=False)  # May fail if nothing to commit
    run_command(["repo", "status"], cwd=android_kernel_path)


def prepare_gki_artifacts(outpath: str) -> None:
    """Prepare GKI kernel artifacts including AnyKernel3."""
    print()
    print("=" * 50)
    print("Prepare GKI Kernel artifacts")
    print("=" * 50)

    print("Prepare AnyKernel3")
    anykernel_dir = os.path.join(outpath, 'AnyKernel3')
    if os.path.exists(anykernel_dir):
        shutil.rmtree(anykernel_dir)
    run_command([
        'git', 'clone', 'https://github.com/Kernel-SU/AnyKernel3',
        anykernel_dir
    ])
    if os.path.exists(os.path.join(anykernel_dir, '.git')):
        shutil.rmtree(os.path.join(anykernel_dir, '.git'))
    shutil.copy(os.path.join(outpath, 'Image'), anykernel_dir)


class KernelBuilder:
    """Build kernel with KernelSU integration."""

    def __init__(self, config: KsuBuildConfig):
        self.config = config

    def setup_kernelsu(self) -> None:
        """Integrate KernelSU source into the Android kernel source."""
        print()
        print("=" * 50)
        print("Setup KernelSU")
        print("=" * 50)

        kernelsu_driver_path = os.path.join(self.config.kernel_source_path,
                                            KERNEL_KSU_DRIVER_REL_PATH)
        if os.path.lexists(kernelsu_driver_path):
            print("warning: found ksu driver in kernel source, skip setup!")
            return
        print(
            f"[+] Linking KernelSU driver to {self.config.kernel_source_path}/{KERNEL_DRIVERS_REL_PATH}"
        )
        os.symlink(os.path.join(self.config.ksu_source_path, "kernel"),
                   kernelsu_driver_path)

        print("[+] Add KernelSU driver to Makefile")
        makefile_path = os.path.join(self.config.kernel_source_path,
                                     KERNEL_DRIVERS_MAKEFILE)
        with open(makefile_path, "r") as f:
            content = f.read()
        if "kernelsu" not in content:
            with open(makefile_path, "a") as f:
                f.write("\nobj-$(CONFIG_KSU) += kernelsu/\n")

        print("[+] Add KernelSU driver to Kconfig")
        kconfig_path = os.path.join(self.config.kernel_source_path,
                                    KERNEL_DRIVERS_KCONFIG)
        with open(kconfig_path, "r") as f:
            lines = f.readlines()
        # Find the position before the last 'endmenu'
        insert_pos = None
        for i in range(len(lines) - 1, -1, -1):
            if lines[i].strip() == "endmenu":
                insert_pos = i
                break
        if insert_pos is not None:
            if not any("kernelsu" in line for line in lines):
                lines.insert(insert_pos, 'source "drivers/kernelsu/Kconfig"\n')
                with open(kconfig_path, "w") as f:
                    f.writelines(lines)

        if self.config.debug:
            print("[+] Enable debug features for kernelSU")
            makefile_path = os.path.join(self.config.ksu_source_path,
                                         "kernel/Kbuild")
            with open(makefile_path, "a") as f:
                f.write("\nccflags-y += -DCONFIG_KSU_DEBUG\n")

        # This part is from the GKI workflow
        print("[+] Apply Compilation Patches")
        build_sh_path = os.path.join(self.config.kernel_source_path,
                                     'build/build.sh')
        if not os.path.exists(build_sh_path):
            try:
                ldd_result = run_command(['ldd', '--version'], check=False)
                ldd_output = ldd_result.stdout or ''
                if ldd_output:
                    glibc_version_line = ldd_output.splitlines()[0]
                    glibc_version = glibc_version_line.split()[-1]
                    print(f"GLIBCVERSION: {glibc_version}")
                    if float(glibc_version) >= 2.38:
                        print("Patching resolveBtfids/Makefile")
                        makefile_path = os.path.join(
                            self.config.kernel_source_path,
                            'common/tools/bpf/resolveBtfids/Makefile')
                        if os.path.exists(makefile_path):
                            run_command([
                                'sed', '-i',
                                r'/\$(Q)\$(MAKE) -C \$(SUBCMDSRC) OUTPUT=\$(abspath \$(dir \$@))\/ \$(abspath \$@)/s//$(Q)$(MAKE) -C $(SUBCMDSRC) EXTRACFLAGS="$(CFLAGS)" OUTPUT=$(abspath $(dir $@))\/ $(abspath $@)/',
                                makefile_path
                            ],
                                        check=False)
            except Exception as e:
                print(f"Could not check GLIBC version or patch: {e}")

        run_command(["repo", "status"], cwd=self.config.kernel_source_path)
        print("[+] KernelSU setup done.")

    def _build_legacy_kernel(self) -> None:
        """Build kernel using legacy build system (Android 12, kernel 5.10)."""
        build_env = os.environ.copy()
        build_env[
            'BUILD_CONFIG'] = self.config.build_config_legacy or "common/build.config.gki.aarch64"
        build_env['CC'] = '/usr/bin/ccache clang'
        build_env['DIST_DIR'] = self.config.output_path
        build_env['LTO'] = 'thin'
        cmd = ['bash', 'build/build.sh']
        run_command(cmd, cwd=self.config.kernel_source_path, env=build_env)

    def _build_modern_kernel(self) -> None:
        """Build kernel using modern Bazel build system."""
        build_env = os.environ.copy()
        base_cmd = [
            'tools/bazel', 'run', '--disk_cache=/home/runner/.cache/bazel',
            '--config=fast', '--config=stamp', '--verbose_failures'
        ]
        # Determine LTO settings based on kernel version
        if compare_kernel_versions(self.config.kernel_ver, "6.12") >= 0:
            # Disable LTO for kernel >= 6.12
            base_cmd.append('--lto=none')
            dest_flag = [f'--destdir={self.config.output_path}']
        else:
            base_cmd.append('--lto=thin')
            dest_flag = [f'--dist_dir={self.config.output_path}']

        # Determine build target
        if self.config.build_type == "avd":
            run_target = [
                f'//common-modules/virtual-device:virtual_device_{self.config.build_arch}_dist'
            ]
        else:
            run_target = [f'//common:kernel_{self.config.build_arch}_dist']

        cmd = base_cmd + run_target + ['--'] + dest_flag
        run_command(cmd, cwd=self.config.kernel_source_path, env=build_env)

    def build_kernel(self) -> None:
        """Build the kernel with appropriate build system."""
        print()
        print("=" * 50)
        print("Build kernel")
        print("=" * 50)
        build_sh_path = os.path.join(self.config.kernel_source_path,
                                     'build/build.sh')
        if os.path.exists(build_sh_path):
            self._build_legacy_kernel()
        else:
            self._build_modern_kernel()


def main(args: argparse.Namespace) -> None:
    """Main entry point for the build script."""
    global GITHUB_ENV_FILE_PATH

    # Setup GitHub environment file if specified
    if args.github_env_file:
        GITHUB_ENV_FILE_PATH = args.github_env_file
        print("set github env file to :", GITHUB_ENV_FILE_PATH)

    # Convert paths to absolute
    workspace = args.workspace
    kernel_version = args.kernel_version
    kernel_source_path = path_to_absolute(workspace, args.kernel_source_path)
    ksu_dir = path_to_absolute(workspace, args.ksu_source_path)
    outpath = path_to_absolute(workspace, args.outpath)
    print("Kernel Branch :", kernel_version)
    print("kernel source at:", kernel_source_path)
    print("ksu source at:", ksu_dir)

    # Create output directory
    if args.buildtype == 'avd':
        buildout = os.path.join(outpath, "avd")
    elif args.buildtype == 'gki':
        buildout = os.path.join(outpath, "gki")
    else:
        raise ValueError(f"Invalid build type: {args.buildtype}")

    os.makedirs(buildout, exist_ok=True)
    print("build output path is:", buildout)

    # Create build configuration
    config = KsuBuildConfig(kernel_source_path=kernel_source_path,
                            build_arch=args.arch,
                            build_type=args.buildtype,
                            debug=args.debug,
                            kernel_ver=kernel_version,
                            ksu_source_path=ksu_dir,
                            output_path=buildout,
                            build_config_legacy=args.build_legacy_cfg)

    # Initialize builder
    builder = KernelBuilder(config)

    # Calculate and log KernelSU version
    kernelsu_version = calculate_ksu_version(ksu_dir)
    print("KernelSU Version Code:", kernelsu_version)
    write_github_env("ksu_version", kernelsu_version)

    # --- Build Stages ---
    builder.setup_kernelsu()
    clean_workspace(kernel_source_path)
    builder.build_kernel()
    if args.buildtype == 'gki':
        prepare_gki_artifacts(buildout)
    print("outpath tree below:")
    run_command(["tree", "-h"], cwd=outpath)


if __name__ == "__main__":
    args = parse_arguments()
    main(args)
