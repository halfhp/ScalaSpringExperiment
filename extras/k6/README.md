# Benchmarking With K6
A simple docker-compose environment preconfigured to benchmark this project's main Spring app
running on localhost:8080 using K6.

How to Use:

1. Build and run the main Spring app on localhost:8080.  You can either do this manually or via
   the docker-compose.yml file in the root of this project:
   ```bash
    docker-compose -f docker-compose.yml up
    ```
2. Run docker-compose-k6.yml:
   ```bash
   cd extras/k6
   docker-compose -f docker-compose-k6.yml up
   ```

Once started, navigate to [http://localhost:3000](http://localhost:3000) to view the k6 dashboard.
Benchmark results will stream in realtime.

The environment is setup with persistent volumes which allows you to modify either the test script
or the main app and see the changes in performance over time.  Restarting the k6 container will
rebuild the test script and re-run the tests.  Restarting the entire k6 docker-compose environment works too:
