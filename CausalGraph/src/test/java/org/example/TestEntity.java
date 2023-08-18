package org.example;

import org.example.nested.Entity;
import org.example.nested.Playground;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@DisplayName("ConfoundingEntity")
public class TestEntity {


    @Test
    void createEntity() {
        Playground.createEntity();
    }

    @Test
//    @EnabledIf("isCondition")
    @ExtendWith(TestExtension.class)
    void editEntity() {
        Playground.editEntity("Test");
    }


    public boolean isCondition() {
        return false;
    }

    public static class TestExtension implements ExecutionCondition {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            if (false) {
                return ConditionEvaluationResult.enabled("Enabled");
            }
            return ConditionEvaluationResult.disabled("Disabled");
        }
    }
}
