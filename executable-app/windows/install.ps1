# Define the download URLs, file names, and folder name
$downloadUrls = @{
    "jpm.exe" = "https://github.com/kinsleykajiva/J-PM/raw/master/executable-app/windows/jpm.exe"
    "j-pm-server.exe" = "https://github.com/kinsleykajiva/J-PM/raw/master/releases/j-pm-server.exe"
}
$folderName = ".jpm"
$downloadFolderPath = Join-Path -Path $env:USERPROFILE -ChildPath $folderName

# Create the folder if it doesn't exist
if (-not (Test-Path $downloadFolderPath -PathType Container)) {
    New-Item -ItemType Directory -Path $downloadFolderPath | Out-Null
}

# Download the files and add their folder path to the PATH environment variable
foreach ($fileName in $downloadUrls.Keys) {
    $downloadUrl = $downloadUrls[$fileName]
    $downloadFilePath = Join-Path -Path $downloadFolderPath -ChildPath $fileName
    Invoke-WebRequest -Uri $downloadUrl -OutFile $downloadFilePath

    # Add the download folder path to the PATH environment variable
    $oldPath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::User)
    $newPath = "$oldPath;$downloadFolderPath"
    [System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::User)

    # Display a message indicating successful download and PATH update for each file
    Write-Host "✔ File downloaded to: $downloadFilePath"
}

# Display a message indicating successful download and PATH update for both files
Write-Host "✔ jpm app and j-pm-server added to PATH environment variable. Please open a new PowerShell session to use the command."
Write-Host -ForegroundColor Green "✔ To test installation please run jpm -v or jpm --version"
