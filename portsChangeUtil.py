import zipfile
import os


def modify_resource_in_jar(jar_path, resource_path, new_content):

    temp_dir = "temp_jar_extraction"

    with zipfile.ZipFile(jar_path, "r") as jar:
        jar.extractall(temp_dir)

    resource_full_path = os.path.join(temp_dir, resource_path)
    if not os.path.exists(resource_full_path):
        print(f"Resource '{resource_path}' not found in the JAR file.")
        return

    # Modify the resource file content
    with open(resource_full_path, "w") as resource_file:
        resource_file.write(new_content)

    # Now, we need to update the JAR file
    # Create a new JAR file, preserving the original files and adding the modified one
    with zipfile.ZipFile(jar_path, "w", zipfile.ZIP_DEFLATED) as jar:
        # Walk through the extracted files and add them to the JAR
        for folder_name, subfolders, filenames in os.walk(temp_dir):
            for filename in filenames:
                file_path = os.path.join(folder_name, filename)
                arcname = os.path.relpath(
                    file_path, temp_dir
                )  # Get the relative path inside the JAR
                jar.write(file_path, arcname)

    # Cleanup the temporary directory
    for folder_name, subfolders, filenames in os.walk(temp_dir, topdown=False):
        for filename in filenames:
            os.remove(os.path.join(folder_name, filename))
        os.rmdir(folder_name)

    print(f"Resource '{resource_path}' in '{jar_path}' has been modified successfully.")


# Example usage
jar_file = "D:\\fax\\master\\PTB-FLA_integration\\PTB-FLA_BabelAdapter\\target\\PTBFLA-Babel-adapter-0.0.6.jar"
resource_file = "adapter.conf"  # Path inside the JAR
new_resource_content = "doppelganger.ports=[6000,6002]"  # define remote instance ports

modify_resource_in_jar(jar_file, resource_file, new_resource_content)
