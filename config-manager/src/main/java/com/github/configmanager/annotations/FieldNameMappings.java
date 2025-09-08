package com.github.configmanager.annotations;

import javax.naming.Name;

public class FieldNameMappings implements NameMappings {
    @Override
    public String nameMapping(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(name.charAt(0)));

        for (int i = 1; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_').append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }


}
