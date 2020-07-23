package cimc.raffles.mybatis.enumeration;

import java.util.Locale;
import java.util.Optional;

public enum Direction 
{
	ASC, DESC ;
	
	
	public boolean isAscending() {
		return this.equals(ASC);
	}

	/**
	 * Returns whether the direction is descending.
	 *
	 * @return
	 * @since 1.13
	 */
	public boolean isDescending() {
		return this.equals(DESC);
	}

	/**
	 * Returns the {@link Direction} enum for the given {@link String} value.
	 *
	 * @param value
	 * @throws IllegalArgumentException in case the given value cannot be parsed into an enum value.
	 * @return
	 */
	public static Direction fromString(String value) {

		try {
			return Direction.valueOf(value.toUpperCase(Locale.US));
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format(
					"Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
		}
	}

	/**
	 * Returns the {@link Direction} enum for the given {@link String} or null if it cannot be parsed into an enum
	 * value.
	 *
	 * @param value
	 * @return
	 */
	public static Optional<Direction> fromOptionalString(String value) {

		try {
			return Optional.of(fromString(value));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
