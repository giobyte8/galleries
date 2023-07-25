# Revert all migrations and applied them again
# for integration tests database

./matw -c migrations-test.yml rollback
./matw -c migrations-test.yml migrate

