package org.ttarena.arena_character.model.enums;

import java.util.Map;

public interface Specialization {
    String name();

    Role getRole();

    Map<StatType, Integer> getBaseStats();
}
