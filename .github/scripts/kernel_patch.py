import argparse
import os
import subprocess
import sys
from dataclasses import dataclass
from typing import List, Optional

KERNEL_COMMON_REL_PATH = "common"
KERNEL_DRIVERS_REL_PATH = "common/drivers"
KERNEL_KSU_DRIVER_REL_PATH = f"{KERNEL_DRIVERS_REL_PATH}/kernelsu"
KERNEL_DRIVERS_MAKEFILE = f"{KERNEL_DRIVERS_REL_PATH}/Makefile"
KERNEL_DRIVERS_KCONFIG = f"{KERNEL_DRIVERS_REL_PATH}/Kconfig"
KSU_KERNEL_KBUILD = "kernel/Kbuild"


@dataclass
class KsuPatchConfig:
    """Configuration for KernelSU source integration."""

    kernel_source_path: str
    debug: bool
    ksu_source_path: str
    image_label: Optional[str]


def parse_arguments() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Integrate KernelSU into Android kernel sources."
    )
    parser.add_argument(
        "--debug", action="store_true", help="Enable debug features for KernelSU."
    )
    parser.add_argument(
        "--kernel-source-path",
        type=str,
        required=True,
        help="Path to the Android kernel source.",
    )
    parser.add_argument(
        "--ksu-source-path",
        type=str,
        required=True,
        help="Path to the KernelSU source.",
    )
    parser.add_argument(
        "--workspace", type=str, required=True, help="Path to the workspace."
    )
    parser.add_argument("--image-label", type=str, help="GKI snapshot image label.")
    return parser.parse_args()


def path_to_absolute(base: str, target: str) -> str:
    """Convert a relative path to absolute based on the given base."""
    if not os.path.exists(base):
        raise FileNotFoundError(f"Base directory does not exist: {base}")
    if os.path.isabs(target):
        return target
    return os.path.abspath(os.path.join(base, target))


def run_command(
    command: List[str],
    cwd: Optional[str] = None,
    check: bool = True,
    env: Optional[dict] = None,
) -> subprocess.CompletedProcess:
    """Run a command and stream its output."""
    print(f"Executing: {' '.join(command)}", flush=True)
    try:
        process = subprocess.Popen(
            command,
            cwd=cwd,
            text=True,
            env=env or os.environ.copy(),
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            bufsize=1,
            universal_newlines=True,
        )
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
            raise subprocess.CalledProcessError(returncode, command, output=full_output)

        return subprocess.CompletedProcess(
            command, returncode, stdout=full_output, stderr=None
        )

    except FileNotFoundError:
        print(f"Command not found: {command[0]}")
        raise
    except PermissionError:
        print(f"Permission denied when executing: {' '.join(command)}")
        raise
    except subprocess.CalledProcessError as exc:
        print(f"\nCommand failed with exit code {exc.returncode}: {' '.join(command)}")
        sys.exit(exc.returncode)
    except Exception as exc:
        print(f"Unexpected error executing command: {exc}")
        raise


def read_lines(file_path: str) -> List[str]:
    with open(file_path, "r", encoding="utf-8") as file:
        return file.readlines()


def write_lines(file_path: str, lines: List[str]) -> None:
    with open(file_path, "w", encoding="utf-8") as file:
        file.writelines(lines)


def append_line_once(file_path: str, line: str) -> None:
    """Append a line to a file only when an exact line is missing."""
    lines = read_lines(file_path)
    normalized_line = line.rstrip("\n")
    if any(existing.rstrip("\n") == normalized_line for existing in lines):
        return
    if lines and not lines[-1].endswith("\n"):
        lines[-1] += "\n"
    lines.append(line if line.endswith("\n") else f"{line}\n")
    write_lines(file_path, lines)


def append_marked_line_once(file_path: str, marker: str, line: str) -> None:
    """Append a managed line even if the same line exists in another block."""
    lines = read_lines(file_path)
    if any(existing.rstrip("\n") == marker for existing in lines):
        return
    if lines and not lines[-1].endswith("\n"):
        lines[-1] += "\n"
    lines.append(f"{marker}\n")
    lines.append(line if line.endswith("\n") else f"{line}\n")
    write_lines(file_path, lines)


def find_existing_file(base_path: str, candidates: List[str]) -> str:
    """Return the first existing candidate path under base_path."""
    for candidate in candidates:
        file_path = os.path.join(base_path, candidate)
        if os.path.exists(file_path):
            return file_path
    joined = ", ".join(candidates)
    raise FileNotFoundError(
        f"Could not find any expected file under {base_path}: {joined}"
    )


class KernelPatcher:
    """Integrate KernelSU into an Android kernel source tree."""

    def __init__(self, config: KsuPatchConfig):
        self.config = config

    def is_avd_x86_64(self) -> bool:
        image_label = self.config.image_label or ""
        return image_label.startswith("avd-") and (
            "-x64-" in image_label or image_label.endswith("-x64")
        )

    def setup_kernelsu_driver(self) -> None:
        kernelsu_driver_path = os.path.join(
            self.config.kernel_source_path, KERNEL_KSU_DRIVER_REL_PATH
        )
        expected_driver_target = os.path.join(self.config.ksu_source_path, "kernel")

        if os.path.islink(kernelsu_driver_path):
            current_target = os.readlink(kernelsu_driver_path)
            current_target = os.path.abspath(
                os.path.join(os.path.dirname(kernelsu_driver_path), current_target)
            )
            if current_target != os.path.abspath(expected_driver_target):
                raise RuntimeError(
                    f"Unexpected KernelSU driver symlink target: {current_target}"
                )
            print("[+] KernelSU driver symlink already exists")
        elif os.path.lexists(kernelsu_driver_path):
            print("[+] KernelSU driver already exists in kernel source")
        else:
            print(
                f"[+] Linking KernelSU driver to {self.config.kernel_source_path}/{KERNEL_DRIVERS_REL_PATH}"
            )
            os.symlink(expected_driver_target, kernelsu_driver_path)

    def setup_kernel_build_files(self) -> None:
        print("[+] Add KernelSU driver to Makefile")
        append_line_once(
            os.path.join(self.config.kernel_source_path, KERNEL_DRIVERS_MAKEFILE),
            "obj-$(CONFIG_KSU) += kernelsu/\n",
        )

        print("[+] Add KernelSU driver to Kconfig")
        kconfig_path = os.path.join(
            self.config.kernel_source_path, KERNEL_DRIVERS_KCONFIG
        )
        lines = read_lines(kconfig_path)
        if any("drivers/kernelsu/Kconfig" in line for line in lines):
            return

        insert_pos = None
        for index in range(len(lines) - 1, -1, -1):
            if lines[index].strip() == "endmenu":
                insert_pos = index
                break
        if insert_pos is None:
            raise RuntimeError(f"Could not find endmenu in {kconfig_path}")

        lines.insert(insert_pos, 'source "drivers/kernelsu/Kconfig"\n')
        write_lines(kconfig_path, lines)

    def setup_kbuild_flags(self) -> None:
        kbuild_path = os.path.join(self.config.ksu_source_path, KSU_KERNEL_KBUILD)

        if self.config.debug:
            print("[+] Enable KernelSU debug ccflag")
            append_marked_line_once(
                kbuild_path,
                "# KernelSU CI: force debug logging",
                "ccflags-y += -DCONFIG_KSU_DEBUG\n",
            )

        if self.is_avd_x86_64():
            print("[+] Enable KSU_X86_PATCH_SYSCALL_DISPATCHER ccflag")
            append_marked_line_once(
                kbuild_path,
                "# KernelSU CI: force x86 syscall dispatcher patching",
                "ccflags-y += -DCONFIG_KSU_X86_PATCH_SYSCALL_DISPATCHER=1\n",
            )

    def apply_compilation_workarounds(self) -> None:
        print("[+] Apply compilation workarounds")
        build_sh_path = os.path.join(self.config.kernel_source_path, "build/build.sh")
        if os.path.exists(build_sh_path):
            return

        try:
            ldd_result = run_command(["ldd", "--version"], check=False)
            ldd_output = ldd_result.stdout or ""
            if not ldd_output:
                return

            glibc_version_line = ldd_output.splitlines()[0]
            glibc_version = glibc_version_line.split()[-1]
            print(f"GLIBCVERSION: {glibc_version}")
            if float(glibc_version) < 2.38:
                return

            print("Patching resolveBtfids/Makefile")
            makefile_path = os.path.join(
                self.config.kernel_source_path,
                "common/tools/bpf/resolveBtfids/Makefile",
            )
            if os.path.exists(makefile_path):
                run_command(
                    [
                        "sed",
                        "-i",
                        r'/\$(Q)\$(MAKE) -C \$(SUBCMDSRC) OUTPUT=\$(abspath \$(dir \$@))\/ \$(abspath \$@)/s//$(Q)$(MAKE) -C $(SUBCMDSRC) EXTRACFLAGS="$(CFLAGS)" OUTPUT=$(abspath $(dir $@))\/ $(abspath $@)/',
                        makefile_path,
                    ],
                    check=False,
                )
        except Exception as exc:
            print(f"Could not check GLIBC version or patch: {exc}")

    def setup_kernelsu(self) -> None:
        """Integrate KernelSU source into the Android kernel source."""
        print()
        print("=" * 50)
        print("Setup KernelSU")
        print("=" * 50)

        self.setup_kernelsu_driver()
        self.setup_kernel_build_files()
        self.setup_kbuild_flags()
        self.apply_compilation_workarounds()

        run_command(["repo", "status"], cwd=self.config.kernel_source_path, check=False)
        print("[+] KernelSU integration done.")


def clean_workspace(android_kernel_path: str) -> None:
    """Commit the integration changes so the kernel tree is clean for upstream builds."""
    print()
    print("=" * 50)
    print("Make working directory clean")
    print("=" * 50)

    common_dir = os.path.join(android_kernel_path, KERNEL_COMMON_REL_PATH)
    git_env = os.environ.copy()
    git_env.update(
        {
            "GIT_AUTHOR_NAME": "KernelSUBot",
            "GIT_AUTHOR_EMAIL": "kernelsu@example.invalid",
            "GIT_COMMITTER_NAME": "KernelSUBot",
            "GIT_COMMITTER_EMAIL": "kernelsu@example.invalid",
        }
    )
    run_command(["git", "add", "-A"], cwd=common_dir)
    run_command(
        ["git", "commit", "-m", "Add KernelSU"],
        cwd=common_dir,
        env=git_env,
        check=False,
    )
    run_command(["repo", "status"], cwd=android_kernel_path, check=False)


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
        KsuPatchConfig(
            kernel_source_path=kernel_source_path,
            debug=args.debug,
            ksu_source_path=ksu_dir,
            image_label=args.image_label,
        )
    )
    patcher.setup_kernelsu()
    clean_workspace(kernel_source_path)


if __name__ == "__main__":
    main(parse_arguments())
