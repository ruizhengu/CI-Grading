# JSON Causal Testing Framework Frontend

The JSON frontend allows Causal Tests and parameters to be specified in JSON to allow for tests to be quickly written
whilst retaining the flexibility of the Causal Testing Framework (CTF). 

An example is provided in `examples/poisson` which will be walked through in this README to better understand
the framework

`examples/poisson/run_causal_tests.py` contains python code written by the user to implement scenario specific features
such as:
1. Custom Estimators
2. Causal Variable specification
3. Causal test case outcomes
4. Meta constraint functions
5. Mapping JSON distributions, effects, and estimators to python objects

Use case specific information is also declared here such as the paths to the relevant files needed for the tests.

`examples/poisson/causal_tests.json` is the JSON file that allows for the easy specification of multiple causal tests.
Each test requires:
1. Test name
2. Mutations
3. Estimator
4. Estimate_type
5. Effect modifiers
6. Expected effects
7. Skip: boolean that if set true the test won't be executed and will be skipped

To run the JSON frontend example from the root directory of the project, use: 
`python examples\poisson\run_causal_tests.py --data_path="examples\poisson\data.csv" --dag_path="examples\poisson\dag.dot" --json_path="examples\poisson\causal_tests.json"`

A failure flag `-f` can be specified to stop the framework running if a test is failed:
`python examples\poisson\run_causal_tests.py -f --data_path="examples\poisson\data.csv" --dag_path="examples\poisson\dag.dot" --json_path="examples\poisson\causal_tests.json"`

There are two main outputs of this frontend, both are controlled by the logging module. Firstly outputs are printed to stdout (terminal).
Secondly a log file is produced, by default a file called `json_frontend.log` is produced in the directory the script is called from.

The behaviour of where the log file is produced and named can be altered with the --log_path argument:
`python examples\poisson\run_causal_tests.py -f --data_path="examples\poisson\data.csv" --dag_path="examples\poisson\dag.dot" --json_path="examples\poisson\causal_tests.json --log_path="example_directory\logname.log"`