/***************************************************************************
 * File:  AcademicStudentClub.java Course materials (23S) CST 8277
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @date August 28, 2022
 * 
 * Updated by:  Group 11
 *   041060762, Shu-Han Han
 *   041060761, Wan-Hsuan Lee
 *   041061567, Syedmoinuddi Hassan
 *   041066323, Gurarman Singh
 *
 */
package acmecollege.entity;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

// ASC01 - Add missing annotations, please see Week 9 slides page 15.  Value 1 is academic and value 0 is non-academic.
@Entity
@DiscriminatorValue("1")
public class AcademicStudentClub extends StudentClub implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public AcademicStudentClub() {
		super(true);
	}
	
	public AcademicStudentClub(String clubName) {
		this();
		setName(clubName);
	}
	
}
