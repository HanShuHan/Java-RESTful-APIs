/**
 * File:  SecurityRoleSerializer.java
 * Course materials (23S) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author Mike Norman
 *
 */
package acmecollege.rest.serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import acmecollege.entity.SecurityRole;

public class SecurityRoleSerializer extends StdSerializer<Set<SecurityRole>> implements Serializable {

	private static final long serialVersionUID = 1L;

	public SecurityRoleSerializer() {
		this(null);
	}

	public SecurityRoleSerializer(Class<Set<SecurityRole>> t) {
		super(t);
	}

	/**
	 * This is to prevent back and forth serialization between many-to-many
	 * relations.<br>
	 * This is done by setting the relation to null.
	 */
	@Override
	public void serialize(Set<SecurityRole> originalRoles, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Set<SecurityRole> hollowRoles = new HashSet<>();
		
		originalRoles.stream().forEach(originalRole -> {
			SecurityRole hollowRole = new SecurityRole();
			
			hollowRole.setId(originalRole.getId());
			hollowRole.setRoleName(originalRole.getRoleName());
			hollowRole.setUsers(null);
			
			hollowRoles.add(hollowRole);
		});
		
		gen.writeObject(hollowRoles);
	}

	
}