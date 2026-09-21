package bd.gov.govos.domain.enums;

/**
 * Hierarchical access levels for GovOS Bangladesh.
 * Higher privilege = lower level number.
 */
public enum UserRole {
	PRIME_MINISTER(1, "Prime Minister — national oversight"),
	CABINET_MINISTER(2, "Cabinet Minister — ministry scope"),
	SECRETARY(3, "Secretary — ministry operations"),
	DEPARTMENT_OFFICER(4, "Department — daily operations"),
	DISTRICT_ADMIN(5, "District Administration — daily reporting"),
	FIELD_OFFICER(6, "Field Officer — submit reports");

	private final int level;
	private final String description;

	UserRole(int level, String description) {
		this.level = level;
		this.description = description;
	}

	public int getLevel() {
		return level;
	}

	public String getDescription() {
		return description;
	}

	public boolean canViewNational() {
		return this == PRIME_MINISTER;
	}

	public boolean canViewMinistry(Long ministryId, Long userMinistryId) {
		if (this == PRIME_MINISTER) {
			return true;
		}
		return ministryId != null && ministryId.equals(userMinistryId);
	}
}
