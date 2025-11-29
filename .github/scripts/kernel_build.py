import os
import subprocess
import sys
import argparse
import shutil

ksuVerMajor = 3

buildTypes = ['gki', 'avd']

githubEnvFilePath = ""


def arg_parser():
    parser = argparse.ArgumentParser(description="Build Kernel for KernelSU.")
    parser.add_argument("--arch",
                        type=str,
                        help="The architecture to build for (aarch64/x86_64).")
    parser.add_argument("--buildtype",
                        type=str,
                        required=True,
                        help="what to build")
    parser.add_argument("--outpath",
                        type=str,
                        default="KernelSUbuild",
                        help="build artifacts path,relative to workspace")
    parser.add_argument("--workspace",
                        type=str,
                        required=True,
                        help="Path to the workspace.")
    parser.add_argument("--kernel-source-path",
                        type=str,
                        required=True,
                        help="Path to the GKI Android kernel source.")
    parser.add_argument("--kernel-version",
                        type=str,
                        required=True,
                        help="e.g. android12-5.10")
    parser.add_argument("--debug",
                        action='store_true',
                        help="Enable debug features for the kernel.")
    parser.add_argument(
        "--github-env-file",
        type=str,
        help="Path to the GitHub environment file to output variables.")
    args = parser.parse_args()
    if args.buildtype not in buildTypes:
        print("buildtypes must in", buildTypes)
        sys.exit(1)
    return args


def run_command(command, cwd=None, check=True, env=None):
    """A helper function to run a command and exit if it fails."""
    print(f"Executing: {' '.join(command)}", flush=True)
    try:
        process = subprocess.Popen(
            command,
            cwd=cwd,
            text=True,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
        )

        output = []
        for line in iter(process.stdout.readline, ""):
            sys.stdout.write(line)
            output.append(line)

        process.wait()
        returncode = process.returncode
        full_output = "".join(output)

        if check and returncode != 0:
            raise subprocess.CalledProcessError(returncode,
                                                command,
                                                output=full_output)

        class CompletedProcess:

            def __init__(self, stdout, returncode):
                self.stdout = stdout
                self.returncode = returncode

        return CompletedProcess(full_output, returncode)

    except subprocess.CalledProcessError as e:
        # The command's output was already streamed.
        print(
            f"\nCommand failed with exit code {e.returncode}: {' '.join(command)}"
        )
        sys.exit(e.returncode)


def write_gh_env(key: str, value: str):
    if not githubEnvFilePath:
        return
    print(f"Writing to GitHub env file: {githubEnvFilePath}")
    with open(githubEnvFilePath, "a") as f:
        f.write("{}={}\n".format(key, value))


def calc_ksu_version(kernelsu_dir: str):
    result = run_command(["git", "rev-list", "--count", "HEAD"],
                         cwd=kernelsu_dir,
                         check=True)
    kernelsu_version = int(result.stdout.strip()) + 10000 * ksuVerMajor
    print(f"KernelSU VERSION: {kernelsu_version}")
    return str(kernelsu_version)


def setup_kernelsu(kernelsu_dir: str, android_kernel_dir: str, debug: bool):
    """Integrate KernelSU source into the Android kernel source."""
    print()
    print("=" * 50)
    print("Setup KernelSU")
    print("=" * 50)

    kernelsu_driver_path = os.path.join(android_kernel_dir,
                                        "common/drivers/kernelsu")
    if os.path.lexists(kernelsu_driver_path):
        print("warning: found ksu driver in kernel source, skip setup!")
        return
    print(
        f"[+] Linking KernelSU driver to {android_kernel_dir}/common/drivers")
    os.symlink(os.path.join(kernelsu_dir, "kernel"), kernelsu_driver_path)

    print("[+] Add KernelSU driver to Makefile")
    with open(os.path.join(android_kernel_dir, "common/drivers/Makefile"),
              "r+") as f:
        content = f.read()
        if "kernelsu" not in content:
            f.write("\nobj-$(CONFIG_KSU) += kernelsu/\n")

    with open(os.path.join(android_kernel_dir, "common/drivers/Kconfig"),
              "r+") as f:
        content = f.read()
        if "kernelsu" not in content:
            lines = content.splitlines()
            for i in range(len(lines) - 1, -1, -1):
                if lines[i].strip() == "endmenu":
                    lines.insert(i, 'source "drivers/kernelsu/Kconfig"')
                    break
            f.seek(0)
            f.write("\n".join(lines) + "\n")

    if debug:
        print("[+] Enable debug features for kernelSU")
        with open(os.path.join(kernelsu_dir, "kernel/Makefile"), "a") as f:
            f.write("\nccflags-y += -DCONFIG_KSU_DEBUG\n")

    # This part is from the GKI workflow
    print("[+] Apply Compilation Patches")
    if not os.path.exists(os.path.join(android_kernel_dir, 'build/build.sh')):
        try:
            ldd_output = run_command(['ldd', '--version'],
                                     check=False).stdout or ''
            glibc_version = ldd_output.splitlines()[0].split()[-1]
            print(f"GLIBC_VERSION: {glibc_version}")
            if float(glibc_version) >= 2.38:
                print("Patching resolve_btfids/Makefile")
                makefile_path = os.path.join(
                    android_kernel_dir,
                    'common/tools/bpf/resolve_btfids/Makefile')
                if os.path.exists(makefile_path):
                    run_command([
                        'sed', '-i',
                        r'/\$(Q)\$(MAKE) -C \$(SUBCMD_SRC) OUTPUT=\$(abspath \$(dir \$@))\/ \$(abspath \$@)/s//$(Q)$(MAKE) -C $(SUBCMD_SRC) EXTRA_CFLAGS="$(CFLAGS)" OUTPUT=$(abspath $(dir $@))\/ $(abspath $@)/',
                        makefile_path
                    ],
                                check=False)
        except Exception as e:
            print(f"Could not check GLIBC version or patch: {e}")

    run_command(["repo", "status"], cwd=android_kernel_dir)
    print("[+] KernelSU setup done.")



def clean_workspace(android_kernel_dir: str):
    """Clean the workspace and commit changes."""
    print()
    print("=" * 50)
    print("Make working directory clean")
    print("=" * 50)

    run_command(
        ["git", "config", "--global", "user.email", "bot@kernelsu.org"])
    run_command(["git", "config", "--global", "user.name", "KernelSUBot"])
    common_dir = os.path.join(android_kernel_dir, "common")
    run_command(["git", "add", "-A"], cwd=common_dir)
    run_command(["git", "commit", "-a", "-m", "Add KernelSU"],
                cwd=common_dir,
                check=False)  # May fail if nothing to commit
    run_command(["repo", "status"], cwd=android_kernel_dir)


def build_avd_kernel(android_kernel_dir: str, arch: str, kernelVersion: str,
                     outpath: str):
    print()
    print("=" * 50)
    print("Build avd kernel")
    print("=" * 50)
    if not arch:
        print("must specific a arch for avd!!")
        sys.exit(1)
    build_env = os.environ.copy()

    base_cmd = [
        'tools/bazel', 'run', '--disk_cache=/home/runner/.cache/bazel',
        '--config=fast', '--config=stamp', '--verbose_failures'
    ]
    base_cmd.append(
        f"//common-modules/virtual-device:virtual_device_{arch}_dist")

    if kernelVersion == "android-16.1-avd_x86_64":
        base_cmd.extend(["--", f"--destdir={outpath}"])
    else:
        base_cmd.extend(["--", f"--dist_dir={outpath}"])
    run_command(base_cmd, cwd=android_kernel_dir, env=build_env)


def build_gki_kernel(android_kernel_dir: str, kernelVersion: str,
                     outpath: str):
    """Build the kernel."""
    print()
    print("=" * 50)
    print("Build GKI kernel")
    print("=" * 50)
    build_env = os.environ.copy()

    # a12-5.10
    if os.path.exists(os.path.join(android_kernel_dir, 'build/build.sh')):
        cmd = ['bash', 'build/build.sh']
        build_env['LTO'] = 'thin'
        build_env['BUILD_CONFIG'] = 'common/build.config.gki.aarch64'
        build_env['CC'] = '/usr/bin/ccache clang'
        build_env['DIST_DIR'] = outpath
        run_command(cmd, cwd=android_kernel_dir, env=build_env)
    else:
        base_cmd = [
            'tools/bazel', 'run', '--disk_cache=/home/runner/.cache/bazel',
            '--config=fast', '--config=stamp'
        ]
        if kernelVersion == "android16-6.12":
            # disable lto see : https://android.googlesource.com/kernel/build/+/refs/heads/main/kleaf/docs/lto.md
            lto_config = ['--lto=none']
            dest_flag = [f'--destdir={outpath}']
        else:
            lto_config = ['--lto=thin']
            dest_flag = [f'--dist_dir={outpath}']
        cmd = (base_cmd + lto_config + ['//common:kernel_aarch64_dist', '--'] +
               dest_flag)
        run_command(cmd, cwd=android_kernel_dir, env=build_env)


def prepare_gki_artifacts(outpath: str):
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


def main(args):

    # --- Setup Paths ---
    android_kernel_dir = args.kernel_source_path
    kernelsu_dir = os.path.join(args.workspace, "KernelSU")

    kernelVer = args.kernel_version
    print("Kernel Branch :", kernelVer)
    outpath = args.outpath
    if not os.path.isabs(outpath):
        outpath = os.path.join(args.workspace, outpath)
        outpath = os.path.abspath(outpath)
    print("build output path is:", outpath)

    if args.github_env_file:
        global githubEnvFilePath
        githubEnvFilePath = args.github_env_file
        print("setup github env file to", githubEnvFilePath)

    kernelsu_version = calc_ksu_version(kernelsu_dir)
    write_gh_env("kernelsu_version", kernelsu_version)

    # --- Build Stages ---
    setup_kernelsu(kernelsu_dir, android_kernel_dir, args.debug)
    clean_workspace(android_kernel_dir)

    if args.buildtype == 'avd':
        buildout = os.path.join(outpath, "avd")
        os.makedirs(buildout, exist_ok=True)

        build_avd_kernel(
            android_kernel_dir,
            args.arch,
            kernelVer,
            buildout,
        )

    if args.buildtype == 'gki':
        buildout = os.path.join(outpath, "gki")
        os.makedirs(buildout, exist_ok=True)

        build_gki_kernel(android_kernel_dir, kernelVer, buildout)

        prepare_gki_artifacts(buildout)

    print("outpath tree below:")
    run_command(["tree", "-h"], cwd=outpath)


if __name__ == "__main__":
    args = arg_parser()
    main(args)
