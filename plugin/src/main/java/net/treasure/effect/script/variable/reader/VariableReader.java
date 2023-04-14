package net.treasure.effect.script.variable.reader;

import net.treasure.common.Patterns;
import net.treasure.effect.Effect;
import net.treasure.effect.exception.ReaderException;
import net.treasure.effect.script.ReaderContext;
import net.treasure.effect.script.ScriptReader;
import net.treasure.effect.script.variable.Variable;

public class VariableReader extends ScriptReader<ReaderContext<?>, Variable> {

    @Override
    public Variable read(Effect effect, String type, String line) throws ReaderException {
        var matcher = Patterns.EVAL.matcher(line);

        if (matcher.matches()) {
            int start = matcher.start(), end = matcher.end();

            var variable = matcher.group(1);
            if (!effect.hasVariable(variable)) {
                error(effect, type, line, start, end, (effect.checkPredefinedVariable(variable) ? "Unknown variable" : "You cannot edit pre-defined variables") + ": " + variable);
                return null;
            }

            var o = matcher.group(2);
            var operator = switch (o) {
                case "" -> Variable.Operator.EQUAL;
                case "+" -> Variable.Operator.ADD;
                case "-" -> Variable.Operator.SUBTRACT;
                case "*" -> Variable.Operator.MULTIPLY;
                case "/" -> Variable.Operator.DIVIDE;
                default -> null;
            };
            if (operator == null) {
                error(effect, type, line, start, end, "Invalid operator (" + o + ")");
                return null;
            }
            return new Variable(variable, operator, matcher.group(3));
        }
        error(effect, type, line, "Incorrect variable script usage");
        return null;
    }
}