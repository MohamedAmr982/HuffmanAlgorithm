# script.ps1


$file1 = "C:\Users\3arrows\Downloads\Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf"
$file2 = "C:\Users\3arrows\Downloads\extracted.Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf"


$hash1 = Get-FileHash -Path $file1 -Algorithm SHA256 | Select-Object -ExpandProperty Hash
$hash2 = Get-FileHash -Path $file2 -Algorithm SHA256 | Select-Object -ExpandProperty Hash


if ($hash1 -eq $hash2) {
    Write-Host "Files have the same content."
} else {
    Write-Host "Files have different content."
}
