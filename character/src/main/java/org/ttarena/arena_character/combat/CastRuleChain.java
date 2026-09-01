package org.ttarena.arena_character.combat;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class CastRuleChain {

    private final List<CastRule> rules;

    public CastRuleChain(List<CastRule> rules) {
        this.rules = rules.stream().sorted(Comparator.comparingInt(CastRule::order)).toList();
    }

    public void check(CastContext context) {
        rules.forEach(rule -> rule.check(context));
    }

    public List<CastRule> rules() {
        return rules;
    }
}
