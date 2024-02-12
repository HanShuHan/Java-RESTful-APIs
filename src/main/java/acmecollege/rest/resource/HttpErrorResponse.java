/**
 * File:  HttpErrorResponse.java
 * Course materials (23S) CST 8277
 *
 * @author Teddy Yap
 * @author (original) Mike Norman
 * 
 * Note:  Students do NOT need to change anything in this class.
 *
 */
package acmecollege.rest.resource;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbCreator;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpErrorResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int statusCode;
	private final String reasonPhrase;

	public HttpErrorResponse(Status status) {
		this(status.getStatusCode(), status.getReasonPhrase());
	}
	
	@JsonbCreator
	public HttpErrorResponse(@JsonProperty("status-code") int code, @JsonProperty("reason-phrase") String reasonPhrase) {
		this.statusCode = code;
		this.reasonPhrase = reasonPhrase;
	}

	@JsonProperty("status-code")
	public int getStatusCode() {
		return statusCode;
	}

	@JsonProperty("reason-phrase")
	public String getReasonPhrase() {
		return reasonPhrase;
	}
//
//	public void setStatusCode(int statusCode) {
//		this.statusCode = statusCode;
//	}
//
//	public void setReasonPhrase(String reasonPhrase) {
//		this.reasonPhrase = reasonPhrase;
//	}

}