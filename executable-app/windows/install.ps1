# Define the download URL, file name, and folder name
$downloadUrl = "https://github.com/kinsleykajiva/J-PM/raw/master/executable-app/windows/jpm.exe"
$downloadFileName = "jpm.exe"
$folderName = ".jpm"
$downloadFolderPath = Join-Path -Path $env:USERPROFILE -ChildPath $folderName
$downloadFilePath = Join-Path -Path $downloadFolderPath -ChildPath $downloadFileName

# Create the folder if it doesn't exist
if (-not (Test-Path $downloadFolderPath -PathType Container)) {
    New-Item -ItemType Directory -Path $downloadFolderPath | Out-Null
}

# Download the file
Invoke-WebRequest -Uri $downloadUrl -OutFile $downloadFilePath

# Add the download folder path to the PATH environment variable
$oldPath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::User)
$newPath = "$oldPath;$downloadFolderPath"
[System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::User)

# Display a message indicating successful download and PATH update
Write-Host "File downloaded to: $downloadFilePath"
Write-Host "Folder path added to PATH environment variable. Please open a new PowerShell session to use the command."
