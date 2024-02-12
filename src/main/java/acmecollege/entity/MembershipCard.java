/***************************************************************************
 * File:  MembershipCard.java Course materials (23S) CST 8277
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

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("unused")

/**
 * The persistent class for the membership_card database table.
 */
// MC01 - Add the missing annotations.
// MC02 - Do we need a mapped super class?  If so, which one?
@Entity
@Table(name = "membership_card", catalog = "acmecollege")
@AttributeOverride(name = "id", column = @Column(name = "card_id"))
@NamedQuery(name = MembershipCard.FIND_BY_ID, query = "SELECT mc FROM MembershipCard mc WHERE mc.id = :param1")
@NamedQuery(name = MembershipCard.FIND_BY_ID_FETCH_ALL, query = "SELECT mc FROM MembershipCard mc LEFT JOIN FETCH mc.owner LEFT JOIN FETCH mc.clubMembership WHERE mc.id = :param1")
@NamedQuery(name = MembershipCard.FIND_ALL, query = "SELECT mc FROM MembershipCard mc")
@NamedQuery(name = MembershipCard.FIND_ALL_FETCH_ALL, query = "SELECT mc FROM MembershipCard mc LEFT JOIN FETCH mc.owner LEFT JOIN FETCH mc.clubMembership")
@NamedQuery(name = MembershipCard.COUNT_ALL, query = "SELECT COUNT(mc) FROM MembershipCard mc")
public class MembershipCard extends PojoBase implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String FIND_BY_ID = "MembershipCard.findById";
	public static final String FIND_BY_ID_FETCH_ALL = "MembershipCard.findByIdFetchAll";
	public static final String FIND_ALL = "MembershipCard.findAll";
	public static final String FIND_ALL_FETCH_ALL = "MembershipCard.findAllFetchAll";
	public static final String COUNT_ALL = "MembershipCard.countAll";

	// MC03 - Add annotations for 1:1 mapping. Changes here should cascade.
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "membership_id", referencedColumnName = "membership_id")
	private ClubMembership clubMembership;

	// MC04 - Add annotations for M:1 mapping. Changes here should not cascade.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
	private Student owner;

	// MC05 - Add annotations.
	@Column(name = "signed", columnDefinition = "BIT(1)", nullable = false)
	private byte signed;

	public MembershipCard() {
		super();
	}

	public MembershipCard(Student owner, byte signed) {
		this();
		this.owner = owner;
		setSigned(signed);
//		setSigned(signed > 0);
	}

	public MembershipCard(Student owner, ClubMembership clubMembership, byte signed) {
		this();
		this.owner = owner;
		this.clubMembership = clubMembership;
		setSigned(signed);
//		setSigned(signed > 0);
	}

	@JsonBackReference("clubMembership-card")
	public ClubMembership getClubMembership() {
		return clubMembership;
	}

	public void setClubMembership(ClubMembership clubMembership) {
		this.clubMembership = clubMembership;

//		if (clubMembership != null) {
//			clubMembership.setCard(this);
//		}
	}

//	@JsonIgnore
	@JsonBackReference("student-card")
	public Student getOwner() {
		return owner;
	}

	public void setOwner(Student owner) {
		this.owner = owner;

//		if (owner != null) {
//			owner.getMembershipCards().add(this);
//		}
	}

	public byte getSigned() {
		return signed;
	}

	public void setSigned(byte signed) {
		if (signed > 0) {
			this.signed = 0b0001;
		} else {
			this.signed = 0b0000;
		}
	}

//	public void setSigned(boolean signed) {
//		this.signed = (byte) (signed ? 0b0001 : 0b0000);
//	}

	// Inherited hashCode/equals is sufficient for this entity class

}