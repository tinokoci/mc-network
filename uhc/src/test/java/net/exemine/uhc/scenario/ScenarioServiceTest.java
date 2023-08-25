package net.exemine.uhc.scenario;

import org.junit.jupiter.api.Test;

class ScenarioServiceTest {

    @Test
    void getAllScenarios() {
        int size = 21;
        for (int i = 0; i < size; i += 2) {
            if (i < (size - 1)) {
                System.out.println("assigned " + i + " to " + (i + 1));
            }
        }

        for (int i = 0; i < 10; i++) {
            if (i == 5) return;
            System.out.println(i);
        }
    }
}