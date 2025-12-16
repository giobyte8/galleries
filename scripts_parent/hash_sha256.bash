#
# Hashes first argument using sha256 algorithm

echo $1 | openssl dgst -sha256
