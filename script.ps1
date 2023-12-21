# script.ps1


$file1 = "D:\JavaProjects\HuffmanAlgorithm\src\abc.txt"
$file2 = "D:\JavaProjects\HuffmanAlgorithm\d_test.bin"


$hash1 = Get-FileHash -Path $file1 -Algorithm SHA256 | Select-Object -ExpandProperty Hash
$hash2 = Get-FileHash -Path $file2 -Algorithm SHA256 | Select-Object -ExpandProperty Hash


if ($hash1 -eq $hash2) {
    Write-Host "Files have the same content."
} else {
    Write-Host "Files have different content."
}
