import os

import matplotlib.pyplot as plt
import pandas as pd
from causal_testing.specification.causal_dag import CausalDAG
from causal_testing.specification.variable import Input, Output
from causal_testing.specification.scenario import Scenario
from causal_testing.specification.causal_specification import CausalSpecification
from causal_testing.testing.causal_test_outcome import Positive
from causal_testing.testing.base_test_case import BaseTestCase
from causal_testing.testing.causal_test_case import CausalTestCase
from causal_testing.testing.causal_test_engine import CausalTestEngine
from causal_testing.data_collection.data_collector import ObservationalDataCollector
from causal_testing.testing.estimators import LinearRegressionEstimator

OBSERVATIONAL_DATA_PATH = "observational_data.csv"


def whatever_csv(observational_data_path: str):
    result_dict = {
        'association': {},
        'causation': {}
    }

    past_execution_df = pd.read_csv(observational_data_path)
    _, causal_test_engine, causal_test_case = engine_setup(observational_data_path)

    linear_regression_estimator = LinearRegressionEstimator(
        ('edit_object',), 2, 3,
        {'create_object', 'delete_object'},
        ('grade',),
        df=past_execution_df
    )
    causal_test_result = causal_test_engine.execute_test(linear_regression_estimator, causal_test_case, 'ate')

    no_adjustment_linear_regression_estimator = LinearRegressionEstimator(
        ('edit_object',), 2, 3,
        set(),
        ('grade',),
        df=past_execution_df
    )
    association_test_result = causal_test_engine.execute_test(no_adjustment_linear_regression_estimator,
                                                              causal_test_case, 'ate')

    result_dict['association'] = {
        'ate': association_test_result.test_value.value,
        'cis': association_test_result.confidence_intervals,
        'df': past_execution_df
    }
    result_dict['causation'] = {
        'ate': causal_test_result.test_value.value,
        'cis': causal_test_result.confidence_intervals,
        'df': past_execution_df
    }

    return result_dict


def whatever(observational_data_path: str):
    all_fig, all_axes = plt.subplots(1, 1, figsize=(4, 3), squeeze=False)
    # age_fig, age_axes = plt.subplots(1, 2, sharey=True, sharex=True, figsize=(7, 3), squeeze=False)
    # age_contact_fig, age_contact_axes = plt.subplots(2, 2, sharey=True, sharex=True, figsize=(7, 5))

    all_data_results_dict = whatever_csv(observational_data_path)
    plot_whatever(all_data_results_dict, "All Data", all_fig, all_axes, row=0, col=0)

    # past_execution_df = pd.read_csv(observational_data_path)

    output_base_str = 'output'

    if not os.path.exists(output_base_str):
        os.makedirs(output_base_str)
    all_fig.savefig(os.path.join(output_base_str, "all_executions.pdf"), format="pdf")


def plot_whatever(result_dict, title, figure=None, axes=None, row=None, col=None):
    ate = result_dict['causation']['ate']
    association_ate = result_dict['association']['ate']

    causation_df = result_dict['causation']['df']
    association_df = result_dict['association']['df']

    percentage_ate = round((ate / causation_df['grade'].mean()) * 100, 3)
    association_percentage_ate = round((association_ate / association_df['grade'].mean()) * 100, 3)

    ate_cis = result_dict['causation']['cis']
    association_ate_cis = result_dict['association']['cis']
    percentage_causal_ate_cis = [round(((ci / causation_df['grade'].mean()) * 100), 3) for ci in ate_cis]
    percentage_association_ate_cis = [round(((ci / association_df['grade'].mean()) * 100), 3) for ci in
                                      association_ate_cis]

    percentage_causal_errs = [percentage_ate - percentage_causal_ate_cis[0],
                              percentage_causal_ate_cis[1] - percentage_ate]
    percentage_association_errs = [association_percentage_ate - percentage_association_ate_cis[0],
                                   percentage_association_ate_cis[1] - association_percentage_ate]

    xs = [1, 2]
    ys = [association_percentage_ate, percentage_ate]
    yerrs = [percentage_association_errs, percentage_causal_errs]
    xticks = ['Association', 'Causation']
    print(f"Association ATE: {association_percentage_ate} {percentage_association_ate_cis}")
    print(f"Association executions: {len(association_df)}")
    print(f"Causal ATE: {percentage_ate} {percentage_causal_ate_cis}")
    print(f"Causal executions: {len(causation_df)}")
    axes[row, col].set_ylim(0, 30)
    axes[row, col].set_xlim(0, 3)
    axes[row, col].set_xticks(xs, xticks)
    axes[row, col].set_title(title)
    axes[row, col].errorbar(xs, ys, yerrs, fmt='o', markersize=3, capsize=3, markerfacecolor='red', color='black')
    figure.supylabel(r"\% Change in Grades (ATE)", fontsize=10)


def engine_setup(observational_data_path: str):
    causal_dag = CausalDAG('dag.dot')

    # variables
    create_object = Input('create_object', int)
    edit_object = Input('edit_object', int)
    delete_object = Input('delete_object', int)
    grade = Output('grade', int)

    # scenario
    scenario = Scenario(
        variables={
            create_object,
            edit_object,
            delete_object,
            grade
        },
        constraints={
            create_object.z3 <= 3,
            edit_object.z3 <= 3,
            delete_object.z3 <= 3
        }
    )

    # causal specification
    causal_specification = CausalSpecification(scenario, causal_dag)

    base_test_case = BaseTestCase(
        treatment_variable=edit_object,
        outcome_variable=grade
    )

    causal_test_case = CausalTestCase(
        base_test_case=base_test_case,
        expected_causal_effect=Positive,
        control_value=3,
        treatment_value=2
    )

    data_collector = ObservationalDataCollector(scenario, observational_data_path)

    causal_test_engine = CausalTestEngine(causal_specification, data_collector)

    minimal_adjustment_set = causal_dag.identification(base_test_case)

    return minimal_adjustment_set, causal_test_engine, causal_test_case


if __name__ == '__main__':
    whatever(OBSERVATIONAL_DATA_PATH)
