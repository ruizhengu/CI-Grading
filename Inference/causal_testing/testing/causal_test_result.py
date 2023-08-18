from typing import Any, Union
from dataclasses import dataclass

from causal_testing.specification.variable import Variable


@dataclass
class TestValue:
    """A dataclass to hold both the type and value of a causal test result"""

    type: str
    value: float


class CausalTestResult:
    """A container to hold the results of a causal test case. Every causal test case provides a point estimate of
    the ATE, given a particular treatment, outcome, and adjustment set. Some but not all estimators can provide
    confidence intervals."""

    def __init__(
        self,
        treatment: tuple,
        outcome: tuple,
        treatment_value: Union[int, float, str],
        control_value: Union[int, float, str],
        adjustment_set: set,
        test_value: TestValue,
        confidence_intervals: [float, float] = None,
        effect_modifier_configuration: {Variable: Any} = None,
    ):
        self.treatment = treatment
        self.outcome = outcome
        self.treatment_value = treatment_value
        self.control_value = control_value
        if adjustment_set:
            self.adjustment_set = adjustment_set
        else:
            self.adjustment_set = set()
        self.test_value = test_value
        self.confidence_intervals = confidence_intervals

        if effect_modifier_configuration is not None:
            self.effect_modifier_configuration = effect_modifier_configuration
        else:
            self.effect_modifier_configuration = {}

    def __str__(self):
        base_str = (
            f"Causal Test Result\n==============\n"
            f"Treatment: {self.treatment[0]}\n"
            f"Control value: {self.control_value}\n"
            f"Treatment value: {self.treatment_value}\n"
            f"Outcome: {self.outcome[0]}\n"
            f"Adjustment set: {self.adjustment_set}\n"
            f"{self.test_value.type}: {self.test_value.value}\n"
        )
        confidence_str = ""
        if self.confidence_intervals:
            confidence_str += f"Confidence intervals: {self.confidence_intervals}\n"
        return base_str + confidence_str

    def to_dict(self):
        base_dict = {
            "treatment": self.treatment[0],
            "control_value": self.control_value,
            "treatment_value": self.treatment_value,
            "outcome": self.outcome[0],
            "adjustment_set": self.adjustment_set,
            "test_value": self.test_value,
        }
        if self.confidence_intervals:
            base_dict["ci_low"] = min(self.confidence_intervals)
            base_dict["ci_high"] = max(self.confidence_intervals)
        return base_dict

    def ci_low(self):
        """Return the lower bracket of the confidence intervals."""
        if not self.confidence_intervals:
            return None
        return min(self.confidence_intervals)

    def ci_high(self):
        """Return the higher bracket of the confidence intervals."""
        if not self.confidence_intervals:
            return None
        return max(self.confidence_intervals)

    def summary(self):
        """Summarise the causal test result as an intuitive sentence."""
        print(
            f"The causal effect of changing {self.treatment[0]} = {self.control_value} to "
            f"{self.treatment[0]}' = {self.treatment_value} is {self.test_value.value} (95% confidence intervals: "
            f"{self.confidence_intervals})."
        )
