$ErrorActionPreference = "Stop"

if ($env:OS -ne "Windows_NT") {
    throw "Ce script doit être exécuté sous Windows."
}

$rootDir = $PSScriptRoot
$appName = "CoupeDuMonde"
$distDir = Join-Path $rootDir "dist"
$outDir = Join-Path $rootDir "out"
$appImage = Join-Path $distDir $appName
$archive = Join-Path $distDir "$appName-windows-x64.zip"
$stagingDir = Join-Path ([System.IO.Path]::GetTempPath()) (
    "cdm-package-" + [guid]::NewGuid().ToString("N"))

foreach ($tool in @("javac", "jar", "jpackage")) {
    if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
        throw "$tool est introuvable. Installez un JDK 17+ et ajoutez-le au PATH."
    }
}

$compilerVersion = (& javac -version 2>&1 | Out-String).Trim()
if ($compilerVersion -notmatch "javac\s+(\d+)") {
    throw "Impossible de déterminer la version du compilateur : $compilerVersion"
}
if ([int]$Matches[1] -lt 17) {
    throw "Un JDK 17 ou plus récent est requis. Version détectée : $compilerVersion"
}

try {
    New-Item -ItemType Directory -Path $stagingDir | Out-Null
    New-Item -ItemType Directory -Path $distDir -Force | Out-Null

    if (Test-Path $outDir) {
        Remove-Item -LiteralPath $outDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $outDir | Out-Null

    $sources = @(Get-ChildItem -Path (Join-Path $rootDir "src") `
            -Filter "*.java" -Recurse | ForEach-Object { $_.FullName })
    if ($sources.Count -eq 0) {
        throw "Aucun fichier Java trouvé dans src/."
    }

    & javac -encoding UTF-8 -cp (Join-Path $rootDir "lib/*") `
        -d $outDir @sources
    if ($LASTEXITCODE -ne 0) {
        throw "La compilation Java a échoué."
    }

    $appJar = Join-Path $stagingDir "$appName.jar"
    & jar --create --file $appJar -C $outDir .
    if ($LASTEXITCODE -ne 0) {
        throw "La création du JAR a échoué."
    }

    Copy-Item -LiteralPath (Join-Path $rootDir "lib/mysql-connector-j-26.10.0.jar") `
        -Destination $stagingDir

    if (Test-Path $appImage) {
        Remove-Item -LiteralPath $appImage -Recurse -Force
    }
    if (Test-Path $archive) {
        Remove-Item -LiteralPath $archive -Force
    }

    & jpackage `
        --type app-image `
        --name $appName `
        --app-version 1.0.0 `
        --vendor "CDM_APP" `
        --description "Gestion de la Coupe du Monde 2022" `
        --input $stagingDir `
        --main-jar "$appName.jar" `
        --main-class ui.MainFrame `
        --dest $distDir `
        --java-options "-Dfile.encoding=UTF-8"
    if ($LASTEXITCODE -ne 0) {
        throw "La création de l'application Windows a échoué."
    }

    $launcher = Join-Path $appImage "$appName.exe"
    if (-not (Test-Path $launcher)) {
        throw "Le lanceur attendu n'a pas été créé : $launcher"
    }

    Compress-Archive -Path $appImage -DestinationPath $archive `
        -CompressionLevel Optimal
    Write-Host "Lanceur Windows : $launcher"
    Write-Host "Archive transférable : $archive"
}
finally {
    if (Test-Path $stagingDir) {
        Remove-Item -LiteralPath $stagingDir -Recurse -Force
    }
}
