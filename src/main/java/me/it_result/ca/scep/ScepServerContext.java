/**
 * Copyright 2010 Roman Kisilenko
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.it_result.ca.scep;

import me.it_result.ca.Authorization;
import me.it_result.ca.CA;
import me.it_result.ca.db.Database;

/**
 * @author roman
 *
 */
public class ScepServerContext {

	public static final String CONTEXT_ATTRIBUTE = ScepServerContext.class.getName();
	
	private CA ca;
	private Authorization authorization;
	private Database database;
	
	/**
	 * @param ca
	 * @param authorization
	 */
	public ScepServerContext(CA ca, Authorization authorization, Database database) {
		super();
		this.ca = ca;
		this.authorization = authorization;
		this.database = database;
	}
	
	public Database getDatabase() {
		return database;
	}

	public Authorization getAuthorization() {
		return authorization;
	}
	
	public CA getCA() {
		return ca;
	}
	
	void setAuthorization(Authorization authorization) {
		this.authorization = authorization;
	}
	
}
