/**
 * File:  ACMEColegeService.java
 * Course materials (23S) CST 8277
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
package acmecollege.ejb;

import static acmecollege.utility.MyConstants.DEFAULT_KEY_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.DEFAULT_SALT_SIZE;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static acmecollege.utility.MyConstants.DEFAULT_USER_PREFIX;
import static acmecollege.utility.MyConstants.PARAM1;
import static acmecollege.utility.MyConstants.PROPERTY_ALGORITHM;
import static acmecollege.utility.MyConstants.PROPERTY_ITERATIONS;
import static acmecollege.utility.MyConstants.PROPERTY_KEY_SIZE;
import static acmecollege.utility.MyConstants.PROPERTY_SALT_SIZE;
import static acmecollege.utility.MyConstants.PU_NAME;
import static acmecollege.utility.MyConstants.USER_ROLE;
import static acmecollege.utility.MyConstants.PARAM2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmecollege.entity.ClubMembership;
import acmecollege.entity.Course;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.DurationAndStatus;
import acmecollege.entity.MembershipCard;
import acmecollege.entity.PojoBase;
import acmecollege.entity.Professor;
import acmecollege.entity.SecurityRole;
import acmecollege.entity.SecurityUser;
import acmecollege.entity.Student;
import acmecollege.entity.StudentClub;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger();

	@PersistenceContext(name = PU_NAME)
	protected EntityManager em;

	@Inject
	protected Pbkdf2PasswordHash pbAndjPasswordHash;

	public List<Student> getAllStudents() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Student> cq = cb.createQuery(Student.class);
		cq.select(cq.from(Student.class));

		return em.createQuery(cq).getResultList();
	}

	public Student getStudentById(int id) {
		return em.find(Student.class, id);
	}

	@Transactional
	public <T> T save(T entity) {
		em.persist(entity);

		return entity;
	}

	public <T> T refresh(T entity) {
		em.refresh(entity);

		return entity;
	}

	@Transactional
	public void buildUserForNewStudent(Student newStudent) {
		String userName = DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName();
		boolean isDuplicate = (em.createNamedQuery(SecurityUser.COUNT_BY_USERNAME, Long.class).setParameter(PARAM1, userName)
				.getSingleResult() == 1L);
		if (!isDuplicate) {
			SecurityUser userForNewStudent = new SecurityUser();

			Map<String, String> pbAndjProperties = new HashMap<>();
			pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
			pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
			pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
			pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
			pbAndjPasswordHash.initialize(pbAndjProperties);

			String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
			userForNewStudent.setUsername(userName);
			userForNewStudent.setPwHash(pwHash);
			userForNewStudent.setStudent(newStudent);

			/* ACMECS01 - Use NamedQuery on SecurityRole to find USER_ROLE */
			SecurityRole userRole = em.createNamedQuery(SecurityRole.FIND_BY_ROLE_NAME, SecurityRole.class)
					.setParameter(PARAM1, USER_ROLE).getSingleResult();

			userForNewStudent.setStudent(newStudent);
			userForNewStudent.getRoles().add(userRole);
			em.persist(userForNewStudent);

			newStudent.getSecurityUsers().add(userForNewStudent);
			em.merge(newStudent);

			userRole.getUsers().add(userForNewStudent);
			em.merge(userRole);
		}
	}

	public CourseRegistration findStudentCourseRegistrationBy(int studentId, int courseId) {
		Student student = getStudentById(studentId);
		if (student == null) {
			return null;
		}

		CourseRegistration courseRegistrataion = student.getCourseRegistrations().stream()
				.filter(courseReg -> courseReg.getCourse().getId() == courseId).findFirst().orElse(null);

		return courseRegistrataion;
	}

	public CourseRegistration findStudentCourseRegistrationBy(Student student, int courseId) {
		if (student != null) {
			return findStudentCourseRegistrationBy(student.getId(), courseId);
		} else {
			return null;
		}
	}

	@Transactional
	public Professor setProfessorForStudentCourse(int studentId, int courseId, Professor newProfessor) {
		Student student = getStudentById(studentId);
		if (student == null) {
			return null;
		}
		CourseRegistration courseRegistration = findStudentCourseRegistrationBy(studentId, courseId);
		if (courseRegistration == null) {
			return null;
		}

		Professor updatedProfessor = null;
		if (newProfessor != null) {
			Professor foundProfessor = findSingleEntityBy(Professor.class, Professor.FIND_BY_ID_FETCH_ALL, newProfessor.getId());
			String firstName = newProfessor.getFirstName();
			String lastName = newProfessor.getLastName();
			String department = newProfessor.getDepartment();

			if (foundProfessor != null) {
				if (firstName != null) {
					foundProfessor.setFirstName(firstName);
				}
				if (lastName != null) {
					foundProfessor.setLastName(lastName);
				}
				if (department != null) {
					foundProfessor.setDepartment(department);
				}
				foundProfessor.getCourseRegistrations().add(courseRegistration);

				courseRegistration.setProfessor(foundProfessor);
				em.merge(courseRegistration);

				updatedProfessor = foundProfessor;
			} else if (firstName != null && lastName != null && department != null) {
				newProfessor.getCourseRegistrations().add(courseRegistration);
				em.persist(newProfessor);

				courseRegistration.setProfessor(newProfessor);
				em.merge(courseRegistration);

				updatedProfessor = newProfessor;
			}
		} else {
			courseRegistration.setProfessor(null);
			em.merge(courseRegistration);
		}

		return updatedProfessor;

//		Student studentToBeUpdated = em.find(Student.class, studentId);
//		if (studentToBeUpdated == null) {
//			return null;
//		}
//		AtomicReference<Professor> updatedProf = new AtomicReference<Professor>(null);
//
//		if (studentToBeUpdated != null) { // Student exists
//			Set<CourseRegistration> courseRegistrations = studentToBeUpdated.getCourseRegistrations();
//
//			courseRegistrations.forEach(c -> {
//				if (c.getCourse().getId() == courseId) {
//					if (c.getProfessor() != null) {
//						Professor prof = em.find(Professor.class, c.getProfessor().getId());
//						prof.setProfessor(newProfessor.getFirstName(), newProfessor.getLastName(), newProfessor.getDepartment());
//						em.merge(prof);
//						updatedProf.set(prof);
//					} else {
//						newProfessor.getCourseRegistrations().add(c);
//						c.setProfessor(newProfessor);
//						em.merge(c);
//						updatedProf.set(newProfessor);
//					}
////					else { // Professor does not exist
////						c.setProfessor(newProfessor);
////						em.merge(studentToBeUpdated);
////					}
//				}
//			});
//		}
//
//		return updatedProf.get();
	}

	/**
	 * To update a student
	 * 
	 * @param id                 - id of entity to update
	 * @param studentWithUpdates - entity with updated information
	 * @return Entity with updated information
	 */
	@Transactional
	public Student updateStudentById(int id, Student studentWithUpdates) {
		Student studentToBeUpdated = getStudentById(id);

		if (studentToBeUpdated != null) {
			em.refresh(studentToBeUpdated);
			em.merge(studentWithUpdates);
			em.flush();
		}

		return studentToBeUpdated;
	}

	@Transactional
	public <T, U> int deleteEntityFrom(Class<T> entityClass, SingularAttribute<? super T, U> attribute, Class<U> idClass, U id) {
		int count = 0;

		CriteriaBuilder criBuilder = em.getCriteriaBuilder();
		CriteriaDelete<T> criDelete = criBuilder.createCriteriaDelete(entityClass);
		Root<T> root = criDelete.from(entityClass);
		criDelete.where(criBuilder.equal(root.get(attribute), criBuilder.parameter(idClass, "id")));

		count = em.createQuery(criDelete).setParameter("id", id).executeUpdate();
		return count;
	}

	@Transactional
	public int deleteProfessorById(int pId) {
		int count = 0;
		Professor professor = findProfessorById(pId);
		if (professor != null) {
			try {
				em.remove(professor);
				count = 1; // Successfully deleted
			} catch (Exception e) {
			}
		}
		return count;
	}

	public Professor findProfessorById(int id) {
		return findSingleEntityBy(Professor.class, Professor.FIND_BY_ID_FETCH_ALL, id);
	}

	/**
	 * To delete a student by id
	 * 
	 * @param id - student id to delete
	 */
	@Transactional
	public Student deleteStudentById(int id) {
		Student student = findSingleEntityBy(Student.class, Student.FIND_BY_ID_FETCH_ALL, id);
		if (student != null) {
			Set<CourseRegistration> courseRegistrations = student.getCourseRegistrations();
			// Deletable
			if (courseRegistrations == null || courseRegistrations.size() == 0) {
				Set<MembershipCard> cards = student.getMembershipCards();
				if (cards != null && cards.size() > 0) {
					cards.stream().forEach(card -> {
						card.setOwner(null);
						em.merge(card);
					});
				} // End of reseting card owners to null

				// Delete security users
				List<SecurityUser> securityUsers = findAllBy(SecurityUser.class, SecurityUser.FIND_BY_STUDENT_ID_FETCH_ROLES, id);
				if (securityUsers != null && securityUsers.size() > 0) {
					securityUsers.stream().forEach(securityUser -> {
						Set<SecurityRole> roles = securityUser.getRoles();
						// If security user has one or more roles
						if (roles != null && roles.size() > 0) {
							roles.stream().forEach(role -> {
								role.getUsers().remove(securityUser);
								em.merge(role);
							});
						}
						// Delete security user
						em.remove(securityUser);
					});
				}
				// Delete student
				em.remove(student);
			}
		}
		return student;
	}

	public List<StudentClub> getAllStudentClubs() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<StudentClub> cq = cb.createQuery(StudentClub.class);
		cq.select(cq.from(StudentClub.class));

		return em.createQuery(cq).getResultList();
	}

	// Why not use the build-in em.find? The named query
	// SPECIFIC_STUDENT_CLUB_QUERY_NAME
	// includes JOIN FETCH that we cannot add to the above API
	public StudentClub getStudentClubById(int id) {
		return findSingleEntityBy(StudentClub.class, StudentClub.FIND_BY_ID, id);
	}

	// These methods are more generic.

	public <T> void detach(T entity) {
		em.detach(entity);
		;
	}

	public <T> List<T> findAll(Class<T> entity, String namedQuery) {
		return em.createNamedQuery(namedQuery, entity).getResultList();
	}

	public <T> T findSingleEntityBy(Class<T> entityClass, String namedQuery) {
		T result = null;

		try {
			result = em.createNamedQuery(namedQuery, entityClass).getSingleResult();
		} catch (NoResultException e) {
		}

		return result;
	}

	public <T> T findSingleEntityBy(Class<T> entityClass, String namedQuery, int id) {
		T result = null;

		try {
			result = em.createNamedQuery(namedQuery, entityClass).setParameter(PARAM1, id).getSingleResult();
		} catch (NoResultException e) {
		}

		return result;
	}
	
	public <T> T findSingleEntityBy(Class<T> entityClass, String namedQuery, int id, int id2) {
		T result = null;
		
		try {
			result = em.createNamedQuery(namedQuery, entityClass).setParameter(PARAM1, id).setParameter(PARAM2, id2).getSingleResult();
		} catch (NoResultException e) {
		}
		
		return result;
	}

	public <T> List<T> findAllBy(Class<T> entity, String namedQuery, int id) {
		return em.createNamedQuery(namedQuery, entity).setParameter(PARAM1, id).getResultList();
	}

	public <T> List<T> findAllBy(Class<T> entity, String namedQuery, int id, int id2) {
		return em.createNamedQuery(namedQuery, entity).setParameter(PARAM1, id).setParameter(PARAM2, id2).getResultList();
	}

	@Transactional
	public StudentClub deleteStudentClub(int id) {
		// StudentClub sc = getStudentClubById(id);
		StudentClub sc = findSingleEntityBy(StudentClub.class, StudentClub.FIND_BY_ID, id);

		if (sc != null) {
			Set<ClubMembership> clubMemberships = sc.getClubMemberships();

			if (clubMemberships != null && !clubMemberships.isEmpty()) {
				clubMemberships.stream().forEach(cm -> {
//					cm.setStudentClub(null); // not needed

					if (cm.getCard() != null) {
						MembershipCard c = findSingleEntityBy(MembershipCard.class, MembershipCard.FIND_BY_ID_FETCH_ALL,
								cm.getCard().getId());

						c.setClubMembership(null);
						em.merge(c);

						cm.setCard(null);
						em.merge(cm); // the club membership needs to be updated anyway
					}
				});
			}
			// Removes the student club.
			em.remove(sc);
			return sc;
//			Set<ClubMembership> memberships = sc.getClubMemberships();
//			List<ClubMembership> list = new LinkedList<>();
//			memberships.forEach(list::add);
//			list.forEach(m -> {
//				if (m.getCard() != null) {
//					MembershipCard mc = getById(MembershipCard.class, MembershipCard.ID_CARD_QUERY_NAME, m.getCard().getId());
//					mc.setClubMembership(null);
//				}
//				m.setCard(null);
//				em.merge(m);
//			});
//			em.remove(sc);
//			return sc;
		}

		return null; // if the student club does not exist
	}

	// Please study & use the methods below in your test suites

	public boolean isDuplicated(StudentClub newStudentClub) {
		long count = em.createNamedQuery(StudentClub.COUNT_BY_NAME, Long.class).setParameter(PARAM1, newStudentClub.getName())
				.getSingleResult();

		return (count > 1);
	}

	@Transactional
	public StudentClub updateStudentClubById(int id, StudentClub updatingStudentClub) {
		StudentClub studentClubToBeUpdated = getStudentClubById(id);

		if (studentClubToBeUpdated != null) {
			em.refresh(studentClubToBeUpdated);
			studentClubToBeUpdated.setName(updatingStudentClub.getName());
			LOG.debug(studentClubToBeUpdated.getClubMemberships());
			em.merge(studentClubToBeUpdated);
			em.flush();
		}

		return studentClubToBeUpdated;
	}

	@Transactional
	public <T> T update(T entity) {
		return em.merge(entity);
	}

	public ClubMembership getClubMembershipById(int cmId) {
		return findSingleEntityBy(ClubMembership.class, ClubMembership.FIND_BY_ID_FETCH_ALL, cmId);
	}

	@Transactional
	public ClubMembership updateClubMembership(int id, ClubMembership clubMembershipWithUpdates) {
		ClubMembership clubMembershipToBeUpdated = getClubMembershipById(id);

		if (clubMembershipToBeUpdated != null) {
			em.refresh(clubMembershipToBeUpdated);
			em.merge(clubMembershipWithUpdates);
			em.flush();
		}

		return clubMembershipToBeUpdated;
	}

	@Transactional
	public CourseRegistration addCourseRegistrationBy(int studentId, int courseId) {
		CourseRegistration courseRegistration = null;

		// Student and course existence check
		Student student = findSingleEntityBy(Student.class, Student.FIND_BY_ID_FETCH_ALL, studentId);
		Course course = findSingleEntityBy(Course.class, Course.FIND_BY_ID_FETCH_ALL, courseId);
		if (student != null && course != null) {
			courseRegistration = new CourseRegistration();
			courseRegistration.setStudent(student);
			courseRegistration.setCourse(course);
			save(courseRegistration);
			//
			student.getCourseRegistrations().add(courseRegistration);
			update(student);
			course.getCourseRegistrations().add(courseRegistration);
			update(course);
		}

		return courseRegistration;
	}

	@Transactional
	public CourseRegistration addCourseRegistrationBy(int studentId, int courseId, int professorId)
			throws TransactionRequiredException {
		CourseRegistration courseRegistration = null;

		// Student and course existence check
		Student student = findSingleEntityBy(Student.class, Student.FIND_BY_ID_FETCH_ALL, studentId);
		Course course = findSingleEntityBy(Course.class, Course.FIND_BY_ID_FETCH_ALL, courseId);
		Professor professor = findSingleEntityBy(Professor.class, Professor.FIND_BY_ID_FETCH_ALL, professorId);
		if (student != null && course != null && professor != null) {
			courseRegistration = new CourseRegistration();
			courseRegistration.setStudent(student);
			courseRegistration.setCourse(course);
			courseRegistration.setProfessor(professor);
			save(courseRegistration);
			//
			student.getCourseRegistrations().add(courseRegistration);
			update(student);
			course.getCourseRegistrations().add(courseRegistration);
			update(course);
			professor.getCourseRegistrations().add(courseRegistration);
			update(professor);
		}

		return courseRegistration;
	}

	@Transactional
	public List<CourseRegistration> deleteCourseRegistration() throws TransactionRequiredException {
		List<CourseRegistration> courseRegistrations = findAll(CourseRegistration.class, CourseRegistration.FIND_ALL);
		if (!courseRegistrations.isEmpty()) {
			return delete(courseRegistrations);
		}
		return courseRegistrations;
	}

	@Transactional
	public List<CourseRegistration> deleteCourseRegistrationBy(String namedQuery, int studentId)
			throws TransactionRequiredException {

		List<CourseRegistration> courseRegistrations = findAllBy(CourseRegistration.class, namedQuery, studentId);
		if (!courseRegistrations.isEmpty()) {
			return delete(courseRegistrations);
		}
		return courseRegistrations;
	}

	@Transactional
	public List<CourseRegistration> deleteCourseRegistrationBy(String namedQuery, int studentId, int courseId)
			throws TransactionRequiredException {

		List<CourseRegistration> courseRegistrations = findAllBy(CourseRegistration.class, namedQuery, studentId, courseId);
		if (!courseRegistrations.isEmpty()) {
			return delete(courseRegistrations);
		}
		return courseRegistrations;
	}

	@Transactional
	public List<CourseRegistration> delete(List<CourseRegistration> courseRegistrations) throws TransactionRequiredException {
		//
		if (courseRegistrations != null && !courseRegistrations.isEmpty()) {
			courseRegistrations.stream().forEach(courseRegistration -> {
				// Remove the course reference to the course registration
				Course course = courseRegistration.getCourse();
				Set<CourseRegistration> courseCourseRegistration = course.getCourseRegistrations();
				if (courseCourseRegistration != null) {
					courseCourseRegistration.remove(courseRegistration);
					em.merge(course);
				}
				// Remove the student reference to the course registration
				Student student = courseRegistration.getStudent();
				Set<CourseRegistration> studentCourseRegistration = student.getCourseRegistrations();
				if (studentCourseRegistration != null) {
					studentCourseRegistration.remove(courseRegistration);
					em.merge(course);
				}
				// Remove the professor reference to the course registration
				Professor professor = courseRegistration.getProfessor();
				if (professor != null) {
					Set<CourseRegistration> professorCourseRegistration = professor.getCourseRegistrations();
					if (professorCourseRegistration != null) {
						professorCourseRegistration.remove(courseRegistration);
						em.merge(course);
					}
				}
				// Delete the course registration
				em.remove(courseRegistration);
			});
		}
		return courseRegistrations;
	}

	@Transactional
	public ClubMembership addClubMembershipBy(int clubId, DurationAndStatus durationAndStatus) {
		StudentClub studentClub = null;
		ClubMembership newClubMembership = null;
		if (clubId > 0) {
			studentClub = findSingleEntityBy(StudentClub.class, StudentClub.FIND_BY_ID, clubId);
		}
		// Add a new club membership if the given club id exists.
		if (studentClub != null) {
			// Add the new club membership
			newClubMembership = new ClubMembership();
			newClubMembership.setStudentClub(studentClub);
			newClubMembership.setDurationAndStatus(durationAndStatus);
			save(newClubMembership);
			// Update the student club.
			if (studentClub.getClubMemberships() == null) {
				studentClub.setClubMembership(new HashSet<ClubMembership>());
			}
			studentClub.getClubMemberships().add(newClubMembership);
			update(studentClub);
		}
		return newClubMembership;
	}

	@Transactional
	public int deleteClubMembershipById(int id) throws TransactionRequiredException {
		int count = 0;
		ClubMembership clubMembership = findSingleEntityBy(ClubMembership.class, ClubMembership.FIND_BY_ID_FETCH_ALL, id);
		if (clubMembership != null) {
			// Removes the student club's reference to the club membership
			StudentClub club = clubMembership.getStudentClub();
			Set<ClubMembership> studentClubMemberships = club.getClubMemberships();
			if (studentClubMemberships != null && !studentClubMemberships.isEmpty()) {
				studentClubMemberships.remove(clubMembership);
				update(club);
			}
			// Removes the membership card's reference to the club membership
			MembershipCard card = clubMembership.getCard();
			if (card != null) {
				card.setClubMembership(null);
				update(card);
			}
			// Remove the club membership.
			delete(clubMembership);
			count = 1;
		}
		return count;
	}

	private <T> void delete(T entity) throws IllegalArgumentException, TransactionRequiredException {
		em.remove(entity);
	}

	@Transactional
	public MembershipCard addMembershipCardBy(int studentId, int membershipId, byte signed) {
		MembershipCard newMembershipCard = null;
		// Only when the student id and membership id are greater than zero, then we add
		// a new membership card.
		if (studentId > 0 && membershipId > 0) {
			Student student = findSingleEntityBy(Student.class, Student.FIND_BY_ID, studentId);
			ClubMembership clubembership = findSingleEntityBy(ClubMembership.class, ClubMembership.FIND_BY_ID, membershipId);
			// Add a new membership card only when the referencing student and membership
			// exist
			if (student != null && clubembership != null) {
				newMembershipCard = new MembershipCard(student, clubembership, signed);
				save(newMembershipCard);
				// Updates the student's membership card reference
				Set<MembershipCard> studentMembershipCards = student.getMembershipCards();
				if (studentMembershipCards == null) {
					student.setMembershipCards(new HashSet<>());
				}
				studentMembershipCards.add(newMembershipCard);
				update(student);
				// Updates the club membership's membership card reference
				clubembership.setCard(newMembershipCard);
				update(clubembership);
			}
		}
		return newMembershipCard;
	}

	@Transactional
	public MembershipCard addMembershipCardBy(int studentId, byte signed) {
		MembershipCard newMembershipCard = null;
		// Only when the student id is greater than zero, then we add a new membership
		// card.
		if (studentId > 0) {
			Student student = findSingleEntityBy(Student.class, Student.FIND_BY_ID, studentId);
			// Add a new membership card only when the referencing student exists
			if (student != null) {
				newMembershipCard = new MembershipCard(student, signed);
				save(newMembershipCard);
				// Updates the student's membership card reference
				Set<MembershipCard> studentMembershipCards = student.getMembershipCards();
				if (studentMembershipCards == null) {
					student.setMembershipCards(new HashSet<>());
				}
				studentMembershipCards.add(newMembershipCard);
				update(student);
			}
		}
		return newMembershipCard;
	}

	@Transactional
	public int deleteMembershipCardById(int id) throws TransactionRequiredException {
		int count = 0;
		MembershipCard membershipCard = findSingleEntityBy(MembershipCard.class, MembershipCard.FIND_BY_ID_FETCH_ALL, id);
		if (membershipCard != null) {
			deleteMembershipCard(membershipCard);
			count = 1;
		}
		return count;
	}

	@Transactional
	public List<MembershipCard> deleteAllEntityFrom(Class<MembershipCard> clazz)
			throws IllegalArgumentException, TransactionRequiredException {
		LOG.debug("Service deleting all membership cards...");

		List<MembershipCard> membershipCards = Collections.emptyList();
		long count = findSingleEntityBy(Long.class, MembershipCard.COUNT_ALL);
		if (count > 0) {
			membershipCards = findAll(MembershipCard.class, MembershipCard.FIND_ALL_FETCH_ALL);
			// Delete all the membership cards if the retrieved result list is not empty.
			if (!membershipCards.isEmpty()) {
				membershipCards.stream().forEach(membershipCard -> {
					deleteMembershipCard(membershipCard);
				});
			}
		} // else
		return membershipCards;
	}

	@Transactional
	public MembershipCard deleteMembershipCard(MembershipCard membershipCard)
			throws IllegalArgumentException, TransactionRequiredException {

		// Removes the student's reference to the membership card.
		Student student = membershipCard.getOwner();
		Set<MembershipCard> studentMembershipCards = student.getMembershipCards();
		if (studentMembershipCards == null) {
			student.setMembershipCards(new HashSet<>());
		}
		if (!studentMembershipCards.isEmpty()) {
			studentMembershipCards.remove(membershipCard);
		}
		update(student);
		// If the card's referencing club membership exists, then remove its reference
		// form the club membership
		ClubMembership clubMembership = membershipCard.getClubMembership();
		if (clubMembership != null) {
			clubMembership.setCard(null);
			update(clubMembership);
		}
		// Remove the membership card.
		delete(membershipCard);
		
		return membershipCard;
	}

}