import argparse
import os
import subprocess
import sys
from dataclasses import dataclass
from typing import List, Optional


KERNEL_DRIVERS_REL_PATH = "common/drivers"
KERNEL_KSU_DRIVER_REL_PATH = f"{KERNEL_DRIVERS_REL_PATH}/kernelsu"
KERNEL_DRIVERS_MAKEFILE = f"{KERNEL_DRIVERS_REL_PATH}/Makefile"
KERNEL_DRIVERS_KCONFIG = f"{KERNEL_DRIVERS_REL_PATH}/Kconfig"


@dataclass
class KsuPatchConfig:
    """Configuration for KernelSU source patching."""

    kernel_source_path: str
    debug: bool
    ksu_source_path: str


def parse_arguments() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Patch Android kernel sources with KernelSU changes.")
    parser.add_argument("--debug",
                        action="store_true",
                        help="Enable debug features for the kernel.")
    parser.add_argument("--kernel-source-path",
                        type=str,
                        required=True,
                        help="Path to the Android kernel source.")
    parser.add_argument("--ksu-source-path",
                        type=str,
                        required=True,
                        help="Path to the KernelSU source.")
    parser.add_argument("--workspace",
                        type=str,
                        required=True,
                        help="Path to the workspace.")
    return parser.parse_args()


def path_to_absolute(base: str, target: str) -> str:
    """Convert a relative path to absolute based on the given base."""
    if not os.path.exists(base):
        raise FileNotFoundError(f"Base directory does not exist: {base}")
    if os.path.isabs(target):
        return target
    return os.path.abspath(os.path.join(base, target))


def run_command(command: List[str],
                cwd: Optional[str] = None,
                check: bool = True,
                env: Optional[dict] = None) -> subprocess.CompletedProcess:
    """Run a command and stream its output."""
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
        if process.stdout is None:
            raise RuntimeError("Failed to capture command output")

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

    except FileNotFoundError:
        print(f"Command not found: {command[0]}")
        raise
    except PermissionError:
        print(f"Permission denied when executing: {' '.join(command)}")
        raise
    except subprocess.CalledProcessError as exc:
        print(
            f"\nCommand failed with exit code {exc.returncode}: {' '.join(command)}"
        )
        if env:
            print(f"running env below:\n{env}")
        sys.exit(exc.returncode)
    except Exception as exc:
        print(f"Unexpected error executing command: {exc}")
        raise


def append_line_once(file_path: str, line: str) -> None:
    """Append a line to a file only when it is missing."""
    with open(file_path, "r", encoding="utf-8") as file:
        content = file.read()
    if line.strip() in content:
        return
    with open(file_path, "a", encoding="utf-8") as file:
        if content and not content.endswith("\n"):
            file.write("\n")
        file.write(line)


class KernelPatcher:
    """Patch kernel sources to integrate KernelSU."""

    def __init__(self, config: KsuPatchConfig):
        self.config = config

    def setup_kernelsu(self) -> None:
        """Integrate KernelSU source into the Android kernel source."""
        print()
        print("=" * 50)
        print("Setup KernelSU")
        print("=" * 50)

        kernelsu_driver_path = os.path.join(self.config.kernel_source_path,
                                            KERNEL_KSU_DRIVER_REL_PATH)
        expected_driver_target = os.path.join(self.config.ksu_source_path,
                                              "kernel")

        if os.path.islink(kernelsu_driver_path):
            current_target = os.readlink(kernelsu_driver_path)
            current_target = os.path.abspath(
                os.path.join(os.path.dirname(kernelsu_driver_path), current_target))
            if current_target != os.path.abspath(expected_driver_target):
                raise RuntimeError(
                    f"Unexpected KernelSU driver symlink target: {current_target}")
            print("[+] KernelSU driver symlink already exists")
        elif os.path.lexists(kernelsu_driver_path):
            print("[+] KernelSU driver already exists in kernel source")
        else:
            print(
                f"[+] Linking KernelSU driver to {self.config.kernel_source_path}/{KERNEL_DRIVERS_REL_PATH}"
            )
            os.symlink(expected_driver_target, kernelsu_driver_path)

        print("[+] Add KernelSU driver to Makefile")
        append_line_once(
            os.path.join(self.config.kernel_source_path, KERNEL_DRIVERS_MAKEFILE),
            "obj-$(CONFIG_KSU) += kernelsu/\n")

        print("[+] Add KernelSU driver to Kconfig")
        kconfig_path = os.path.join(self.config.kernel_source_path,
                                    KERNEL_DRIVERS_KCONFIG)
        with open(kconfig_path, "r", encoding="utf-8") as file:
            lines = file.readlines()
        if not any("drivers/kernelsu/Kconfig" in line for line in lines):
            insert_pos = None
            for index in range(len(lines) - 1, -1, -1):
                if lines[index].strip() == "endmenu":
                    insert_pos = index
                    break
            if insert_pos is None:
                raise RuntimeError(f"Could not find endmenu in {kconfig_path}")
            lines.insert(insert_pos, 'source "drivers/kernelsu/Kconfig"\n')
            with open(kconfig_path, "w", encoding="utf-8") as file:
                file.writelines(lines)

        if self.config.debug:
            print("[+] Enable debug features for KernelSU")
            append_line_once(os.path.join(self.config.ksu_source_path,
                                          "kernel/Kbuild"),
                             "ccflags-y += -DCONFIG_KSU_DEBUG\n")

        print("[+] Apply compilation patches")
        build_sh_path = os.path.join(self.config.kernel_source_path,
                                     "build/build.sh")
        if not os.path.exists(build_sh_path):
            try:
                ldd_result = run_command(["ldd", "--version"], check=False)
                ldd_output = ldd_result.stdout or ""
                if ldd_output:
                    glibc_version_line = ldd_output.splitlines()[0]
                    glibc_version = glibc_version_line.split()[-1]
                    print(f"GLIBCVERSION: {glibc_version}")
                    if float(glibc_version) >= 2.38:
                        print("Patching resolveBtfids/Makefile")
                        makefile_path = os.path.join(
                            self.config.kernel_source_path,
                            "common/tools/bpf/resolveBtfids/Makefile")
                        if os.path.exists(makefile_path):
                            run_command([
                                "sed", "-i",
                                r'/\$(Q)\$(MAKE) -C \$(SUBCMDSRC) OUTPUT=\$(abspath \$(dir \$@))\/ \$(abspath \$@)/s//$(Q)$(MAKE) -C $(SUBCMDSRC) EXTRACFLAGS="$(CFLAGS)" OUTPUT=$(abspath $(dir $@))\/ $(abspath $@)/',
                                makefile_path
                            ],
                                        check=False)
            except Exception as exc:
                print(f"Could not check GLIBC version or patch: {exc}")

        run_command(["repo", "status"], cwd=self.config.kernel_source_path)
        print("[+] KernelSU patching done.")


def clean_workspace(android_kernel_path: str) -> None:
    """Commit the patch changes so the kernel tree is clean for upstream builds."""
    print()
    print("=" * 50)
    print("Make working directory clean")
    print("=" * 50)

    common_dir = os.path.join(android_kernel_path, "common")
    git_env = os.environ.copy()
    git_env.update({
        "GIT_AUTHOR_NAME": "KernelSUBot",
        "GIT_AUTHOR_EMAIL": "bot@kernelsu.org",
        "GIT_COMMITTER_NAME": "KernelSUBot",
        "GIT_COMMITTER_EMAIL": "bot@kernelsu.org",
    })
    run_command(["git", "add", "-A"], cwd=common_dir)
    run_command(["git", "commit", "-a", "-m", "Add KernelSU"],
                cwd=common_dir,
                env=git_env,
                check=False)
    run_command(["repo", "status"], cwd=android_kernel_path)


def main(args: argparse.Namespace) -> None:
    """Main entry point for the patch script."""
    workspace = args.workspace
    kernel_source_path = path_to_absolute(workspace, args.kernel_source_path)
    ksu_dir = path_to_absolute(workspace, args.ksu_source_path)

    print("kernel source at:", kernel_source_path)
    print("ksu source at:", ksu_dir)

    kernelsu_version = os.environ.get("ksu_version")
    if kernelsu_version:
        print("KernelSU Version Code:", kernelsu_version)

    patcher = KernelPatcher(
        KsuPatchConfig(kernel_source_path=kernel_source_path,
                       debug=args.debug,
                       ksu_source_path=ksu_dir))
    patcher.setup_kernelsu()
    clean_workspace(kernel_source_path)


if __name__ == "__main__":
    main(parse_arguments())
