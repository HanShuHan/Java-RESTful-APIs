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

import acmecollege.entity.SecurityUser;

public class SecurityUserSerializer extends StdSerializer<Set<SecurityUser>> implements Serializable {

	private static final long serialVersionUID = 1L;

	public SecurityUserSerializer() {
		this(null);
	}

	public SecurityUserSerializer(Class<Set<SecurityUser>> t) {
		super(t);
	}

	/**
	 * This is to prevent back and forth serialization between many-to-many
	 * relations.<br>
	 * This is done by setting the relation to null.
	 */
	@Override
	public void serialize(Set<SecurityUser> originalRoles, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Set<SecurityUser> hollowRoles = new HashSet<>();
		
		originalRoles.stream().forEach(originalRole -> {
			SecurityUser hollowRole = new SecurityUser();
			
			hollowRole.setId(originalRole.getId());
			hollowRole.setUsername(originalRole.getUsername());
			hollowRole.setPwHash(originalRole.getPwHash());
			hollowRole.setStudent(originalRole.getStudent());
			hollowRole.setRoles(null);
			
			hollowRoles.add(hollowRole);
		});
		
		gen.writeObject(hollowRoles);
	}

	
}