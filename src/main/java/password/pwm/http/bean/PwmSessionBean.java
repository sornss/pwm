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

package password.pwm.http.bean;

import password.pwm.config.option.SessionBeanMode;
import password.pwm.error.ErrorInformation;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public abstract class PwmSessionBean implements Serializable {
    public enum Type {
        PUBLIC,
        AUTHENTICATED,
    }

    private String guid;
    private Date timestamp;
    private ErrorInformation lastError;

    public String getGuid() {
        return guid;
    }

    public void setGuid(final String guid) {
        this.guid = guid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public ErrorInformation getLastError() {
        return lastError;
    }

    public void setLastError(final ErrorInformation lastError) {
        this.lastError = lastError;
    }

    public abstract Type getType();

    public abstract Set<SessionBeanMode> supportedModes();
}
