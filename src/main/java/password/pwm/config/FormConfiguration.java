/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2017 The PWM Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package password.pwm.config;

import password.pwm.AppProperty;
import password.pwm.PwmConstants;
import password.pwm.error.ErrorInformation;
import password.pwm.error.PwmDataValidationException;
import password.pwm.error.PwmError;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.java.JsonUtil;
import password.pwm.util.LocaleHelper;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Jason D. Rivard
 */
public class FormConfiguration implements Serializable {
// ------------------------------ FIELDS ------------------------------

    public enum Type {text, email, number, password, random, tel, hidden, date, datetime, time, week, month, url, select, userDN, checkbox}

    private String name;
    private int minimumLength;
    private int maximumLength;
    private Type type = Type.text;
    private boolean required;
    private boolean confirmationRequired;
    private boolean readonly;
    private boolean unique;
    private boolean multivalue;
    private Map<String,String> labels = Collections.singletonMap("", "");
    private Map<String,String> regexErrors = Collections.singletonMap("","");
    private Map<String,String> description = Collections.singletonMap("","");
    private String regex;
    private String placeholder;
    private String javascript;
    private Map<String,String> selectOptions = Collections.emptyMap();

// -------------------------- STATIC METHODS --------------------------

    public static FormConfiguration parseOldConfigString(final String config)
            throws PwmOperationalException
    {
        if (config == null) {
            throw new NullPointerException("config can not be null");
        }

        final FormConfiguration formItem = new FormConfiguration();
        final StringTokenizer st = new StringTokenizer(config, ":");

        // attribute name
        formItem.name = st.nextToken();

        // label
        formItem.labels = Collections.singletonMap("",st.nextToken());

        // type
        {
            final String typeStr = st.nextToken();
            try {
                formItem.type = Type.valueOf(typeStr.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{"unknown type for form config: " + typeStr}));
            }
        }

        //minimum length
        try {
            formItem.minimumLength = Integer.parseInt(st.nextToken());
        } catch (NumberFormatException e) {
            throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{"invalid minimum length type for form config: " + e.getMessage()}));
        }

        //maximum length
        try {
            formItem.maximumLength = Integer.parseInt(st.nextToken());
        } catch (NumberFormatException e) {
            throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{"invalid maximum length type for form config: " + e.getMessage()}));
        }

        //required
        formItem.required = Boolean.TRUE.toString().equalsIgnoreCase(st.nextToken());

        //confirmation
        formItem.confirmationRequired = Boolean.TRUE.toString().equalsIgnoreCase(st.nextToken());

        return formItem;
    }

    public void validate() throws PwmOperationalException {
        if (this.getName() == null || this.getName().length() < 1) {
            throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{" form field name is required"}));
        }

        if (this.getType() == null) {
            throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{" type is required for field " + this.getName()}));
        }

        if (labels == null || this.labels.isEmpty() || this.getLabel(PwmConstants.DEFAULT_LOCALE) == null || this.getLabel(PwmConstants.DEFAULT_LOCALE).length() < 1) {
            throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{" a default label value is required for " + this.getName()}));
        }

        if (this.getRegex() != null && this.getRegex().length() > 0) {
            try {
                Pattern.compile(this.getRegex());
            } catch (PatternSyntaxException e) {
                throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{" regular expression for '" + this.getName() + " ' is not valid: " + e.getMessage()}));
            }
        }

        if (this.getType() == Type.select) {
            if (this.getSelectOptions() == null || this.getSelectOptions().isEmpty()) {
                throw new PwmOperationalException(new ErrorInformation(PwmError.CONFIG_FORMAT_ERROR,null,new String[]{" field '" + this.getName() + " ' is type select, but no select options are defined"}));
            }
        }
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public FormConfiguration() {
        labels = Collections.singletonMap("","");
        regexErrors = Collections.singletonMap("","");
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getName() {
        return name;
    }

    public String getLabel(final Locale locale) {
        return LocaleHelper.resolveStringKeyLocaleMap(locale, labels);
    }

    public Map<String,String> getLabelLocaleMap() {
        return Collections.unmodifiableMap(this.labels);
    }

    public String getRegexError(final Locale locale) {
        return LocaleHelper.resolveStringKeyLocaleMap(locale, regexErrors);
    }

    public String getDescription(final Locale locale) {
        return LocaleHelper.resolveStringKeyLocaleMap(locale, description);
    }

    public Map<String,String> getLabelDescriptionLocaleMap() {
        return Collections.unmodifiableMap(this.description);
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public Type getType() {
        return type;
    }

    public boolean isConfirmationRequired() {
        return confirmationRequired;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isMultivalue() {
        return multivalue;
    }

    public String getRegex() {
        return regex;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getJavascript() {
        return javascript;
    }

    public Map<String,String> getSelectOptions() {
        return Collections.unmodifiableMap(selectOptions);
    }

// ------------------------ CANONICAL METHODS ------------------------

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FormConfiguration)) {
            return false;
        }

        final FormConfiguration parameterConfig = (FormConfiguration) o;

        return !(name != null ? !name.equals(parameterConfig.name) : parameterConfig.name != null);
    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("FormItem: ");
        sb.append(JsonUtil.serialize(this));

        return sb.toString();
    }



// -------------------------- OTHER METHODS --------------------------

    public void checkValue(final Configuration config, final String value, final Locale locale)
            throws PwmDataValidationException, PwmUnrecoverableException {
        //check if value is missing and required.
        if (required && (value == null || value.length() < 1)) {
            final ErrorInformation error = new ErrorInformation(PwmError.ERROR_FIELD_REQUIRED, null, new String[]{getLabel(locale)});
            throw new PwmDataValidationException(error);
        }

        switch (type) {
            case number:
                if (value != null && value.length() > 0) {
                    try {
                        new BigInteger(value);
                    } catch (NumberFormatException e) {
                        final ErrorInformation error = new ErrorInformation(PwmError.ERROR_FIELD_NOT_A_NUMBER, null, new String[]{getLabel(locale)});
                        throw new PwmDataValidationException(error);
                    }
                }
                break;


            case email:
                if (value != null && value.length() > 0) {
                    if (!testEmailAddress(config, value)) {
                        final ErrorInformation error = new ErrorInformation(PwmError.ERROR_FIELD_INVALID_EMAIL, null, new String[]{getLabel(locale)});
                        throw new PwmDataValidationException(error);
                    }
                }
                break;

            default:
                // continue for other types
                break;
        }

        if (value != null && (this.getMinimumLength() > 0) && (value.length() > 0) && (value.length() < this.getMinimumLength())) {
            final ErrorInformation error = new ErrorInformation(PwmError.ERROR_FIELD_TOO_SHORT, null, new String[]{getLabel(locale)});
            throw new PwmDataValidationException(error);
        }

        if (value != null && value.length() > this.getMaximumLength()) {
            final ErrorInformation error = new ErrorInformation(PwmError.ERROR_FIELD_TOO_LONG, null, new String[]{getLabel(locale)});
            throw new PwmDataValidationException(error);
        }

        if (value != null && value.length() > 0 && this.getRegex() != null && this.getRegex().length() > 0) {
            if (!value.matches(this.getRegex())) {
                final String configuredErrorMessage = this.getRegexError(locale);
                final ErrorInformation error = new ErrorInformation(PwmError.ERROR_FIELD_REGEX_NOMATCH, null, configuredErrorMessage, new String[]{getLabel(locale)});
                throw new PwmDataValidationException(error);
            }
        }
    }

    public static List<String> convertToListOfNames(final Collection<FormConfiguration> formConfigurations) {
        if (formConfigurations == null) {
            return Collections.emptyList();
        }
        final ArrayList<String> returnList = new ArrayList<>();
        for (final FormConfiguration formConfiguration : formConfigurations) {
            returnList.add(formConfiguration.getName());
        }
        return returnList;
    }

    /**
     * Return false if an invalid email address is issued.
     * @param config
     * @param address
     * @return
     */
    public static boolean testEmailAddress(final Configuration config, final String address) {
        final String patternStr;
        if (config != null) {
            patternStr = config.readAppProperty(AppProperty.FORM_EMAIL_REGEX);
        } else {
            patternStr = AppProperty.FORM_EMAIL_REGEX.getDefaultValue();
        }

        final Pattern pattern = Pattern.compile(patternStr);
        final Matcher matcher = pattern.matcher(address);
        return matcher.matches();
    }
}
