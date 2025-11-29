import os
import subprocess
import sys
import argparse
import requests
import shutil

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
            raise subprocess.CalledProcessError(
                returncode, command, output=full_output
            )

        class CompletedProcess:
            def __init__(self, stdout, returncode):
                self.stdout = stdout
                self.returncode = returncode
        
        return CompletedProcess(full_output, returncode)

    except subprocess.CalledProcessError as e:
        # The command's output was already streamed.
        print(f"\nCommand failed with exit code {e.returncode}: {' '.join(command)}")
        sys.exit(e.returncode)

def mkbootimg(args):
    """Create boot images from kernel images."""
    print()
    print("="*50)
    print("Create boot images")
    print("="*50)

    for item in os.listdir("."):
        if os.path.isdir(item) and item.startswith("Image"):
            print(f"----- Building {item} -----")
            cwd = item
            
            title = f"kernel-aarch64-{item.replace('Image-', '')}"
            print(f"[+] title: {title}")

            ramdisk_args = []
            if "a12" in args.android_version:
                patch_level = item.split('_')[-1]
                print(f"[+] patch level: {patch_level}")
                
                print("[+] Download prebuilt ramdisk")
                gki_url = f"https://dl.google.com/android/gki/gki-certified-boot-android12-5.10-{patch_level}_r1.zip"
                fallback_url = "https://dl.google.com/android/gki/gki-certified-boot-android12-5.10-2023-01_r1.zip"
                
                response = requests.head(gki_url)
                if response.status_code != 200:
                    print(f"[+] {gki_url} not found, using {fallback_url}")
                    gki_url = fallback_url
                
                zip_path = os.path.join(cwd, "gki-kernel.zip")
                with requests.get(gki_url, stream=True) as r:
                    r.raise_for_status()
                    with open(zip_path, 'wb') as f:
                        shutil.copyfileobj(r.raw, f)

                run_command(["unzip", "gki-kernel.zip"], cwd=cwd)
                os.remove(zip_path)

                print("[+] Unpack prebuilt boot.img")
                boot_img = [f for f in os.listdir(cwd) if f.startswith("boot") and f.endswith(".img")][0]
                run_command([args.unpack_bootimg, f"--boot_img={os.path.join(cwd, boot_img)}", f"--out={os.path.join(cwd, 'out')}"])
                os.remove(os.path.join(cwd, boot_img))
                ramdisk_args = ["--ramdisk", "out/ramdisk", "--os_version", "12.0.0", "--os_patch_level", patch_level]


            print("[+] Building Image.gz")
            run_command([args.gzip, "-n", "-k", "-f", "-9", "Image"], cwd=cwd)
            
            # Create a placeholder Image.lz4 if it doesn't exist, for compatibility with a13 logic
            if not os.path.exists(os.path.join(cwd, "Image.lz4")):
                with open(os.path.join(cwd, "Image.lz4"), 'w') as f:
                    pass # create empty file
            
            common_mkbootimg_args = ["--header_version", "4"]
            common_avb_args = ["add_hash_footer", "--partition_name", "boot", "--partition_size", str(64 * 1024 * 1024), "--algorithm", "SHA256_RSA2048", "--key", args.avb_key]

            images_to_process = [
                ("Image", "boot.img"),
                ("Image.gz", "boot-gz.img"),
                ("Image.lz4", "boot-lz4.img")
            ]

            for kernel_image, output_boot_image in images_to_process:
                # Skip lz4 if the source doesn't exist and we didn't create a placeholder
                if not os.path.exists(os.path.join(cwd, kernel_image)):
                    print(f"[*] Skipping {output_boot_image} as {kernel_image} does not exist.")
                    continue
                
                print(f"[+] Building {output_boot_image}")
                mkbootimg_cmd = [args.mkbootimg] + common_mkbootimg_args + ["--kernel", kernel_image, "--output", output_boot_image] + ramdisk_args
                run_command(mkbootimg_cmd, cwd=cwd)
                
                avb_cmd = [args.avbtool] + common_avb_args + ["--image", output_boot_image]
                run_command(avb_cmd, cwd=cwd)

            print("[+] Compress images")
            for f in os.listdir(cwd):
                if f.startswith("boot") and f.endswith(".img"):
                    new_name = f"{item.replace('Image-', '')}-{f}.gz"
                    run_command([args.gzip, "-n", "-f", "-9", f], cwd=cwd)
                    os.rename(os.path.join(cwd, f"{f}.gz"), os.path.join(cwd, new_name))
            
            print("[+] Images to upload")
            run_command("find . -type f -name \"*.gz\"".split(), cwd=cwd)

def main():
    parser = argparse.ArgumentParser(description="Create boot images for KernelSU.")
    parser.add_argument("--android-version", type=str, required=True, help="Android version, e.g., 'a12' or 'a13'.")
    parser.add_argument("--gzip", type=str, required=True, help="Path to the gzip tool.")
    parser.add_argument("--mkbootimg", type=str, required=True, help="Path to the mkbootimg tool.")
    parser.add_argument("--avbtool", type=str, required=True, help="Path to the avbtool.")
    parser.add_argument("--unpack-bootimg", type=str, help="Path to the unpack_bootimg tool (required for a12).")
    parser.add_argument("--avb-key", type=str, required=True, help="Path to the AVB key.")
    args = parser.parse_args()

    if "a12" in args.android_version and not args.unpack_bootimg:
        sys.exit("--unpack-bootimg is required for Android 12 builds.")

    mkbootimg(args)

if __name__ == "__main__":
    main()
