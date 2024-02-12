/***************************************************************************
 * File:  PojoCompositeListener.java Course materials (23S) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 *
 * Updated by:  Group 11
 *   041060762, Shu-Han Han
 *   041060761, Wan-Hsuan Lee
 *   041061567, Syedmoinuddi Hassan
 *   041066323, Gurarman Singh
 *
 */
package acmecollege.entity;

import java.time.LocalDateTime;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@SuppressWarnings("unused")

public class PojoCompositeListener {

	//  PCL01 - What annotation is used when we want to do something just before object is INSERT'd into database?
	@PrePersist
	public void setCreatedOnDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		LocalDateTime now = LocalDateTime.now();
		//  PCL02 - What member field(s) do we wish to alter just before object is INSERT'd in the database?
		pojoBaseComposite.setCreated(now);
		pojoBaseComposite.setUpdated(now);
	}

	//  PCL03 - What annotation is used when we want to do something just before object is UPDATE'd into database?
	@PreUpdate
	public void setUpdatedDate(PojoBaseCompositeKey<?> pojoBaseComposite) {
		//  PCL04 - What member field(s) do we wish to alter just before object is UPDATE'd in the database?
		LocalDateTime now = LocalDateTime.now();
		pojoBaseComposite.setUpdated(now);
	}

}
