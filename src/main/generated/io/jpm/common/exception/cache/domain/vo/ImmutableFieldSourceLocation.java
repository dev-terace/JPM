package io.jpm.common.exception.cache.domain.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.lang.model.element.Element;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link FieldSourceLocation}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableFieldSourceLocation.builder()}.
 */
@Generated(from = "FieldSourceLocation", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
public final class ImmutableFieldSourceLocation
    extends FieldSourceLocation {
  private final String expression;
  private final String className;
  private final String fieldName;
  private final long lineNumber;
  private final Element element;

  private ImmutableFieldSourceLocation(
      String expression,
      String className,
      String fieldName,
      long lineNumber,
      Element element) {
    this.expression = expression;
    this.className = className;
    this.fieldName = fieldName;
    this.lineNumber = lineNumber;
    this.element = element;
  }

  /**
   * @return The value of the {@code expression} attribute
   */
  @Override
  public String getExpression() {
    return expression;
  }

  /**
   * @return The value of the {@code className} attribute
   */
  @Override
  public String getClassName() {
    return className;
  }

  /**
   * @return The value of the {@code fieldName} attribute
   */
  @Override
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @return The value of the {@code lineNumber} attribute
   */
  @Override
  public long getLineNumber() {
    return lineNumber;
  }

  /**
   * @return The value of the {@code element} attribute
   */
  @Override
  public Element getElement() {
    return element;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link FieldSourceLocation#getExpression() expression} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for expression
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFieldSourceLocation withExpression(String value) {
    String newValue = Objects.requireNonNull(value, "expression");
    if (this.expression.equals(newValue)) return this;
    return new ImmutableFieldSourceLocation(newValue, this.className, this.fieldName, this.lineNumber, this.element);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link FieldSourceLocation#getClassName() className} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for className
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFieldSourceLocation withClassName(String value) {
    String newValue = Objects.requireNonNull(value, "className");
    if (this.className.equals(newValue)) return this;
    return new ImmutableFieldSourceLocation(this.expression, newValue, this.fieldName, this.lineNumber, this.element);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link FieldSourceLocation#getFieldName() fieldName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for fieldName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFieldSourceLocation withFieldName(String value) {
    String newValue = Objects.requireNonNull(value, "fieldName");
    if (this.fieldName.equals(newValue)) return this;
    return new ImmutableFieldSourceLocation(this.expression, this.className, newValue, this.lineNumber, this.element);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link FieldSourceLocation#getLineNumber() lineNumber} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for lineNumber
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFieldSourceLocation withLineNumber(long value) {
    if (this.lineNumber == value) return this;
    return new ImmutableFieldSourceLocation(this.expression, this.className, this.fieldName, value, this.element);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link FieldSourceLocation#getElement() element} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for element
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableFieldSourceLocation withElement(Element value) {
    if (this.element == value) return this;
    Element newValue = Objects.requireNonNull(value, "element");
    return new ImmutableFieldSourceLocation(this.expression, this.className, this.fieldName, this.lineNumber, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableFieldSourceLocation} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableFieldSourceLocation
        && equalTo(0, (ImmutableFieldSourceLocation) another);
  }

  private boolean equalTo(int synthetic, ImmutableFieldSourceLocation another) {
    return expression.equals(another.expression)
        && className.equals(another.className)
        && fieldName.equals(another.fieldName)
        && lineNumber == another.lineNumber
        && element.equals(another.element);
  }

  /**
   * Computes a hash code from attributes: {@code expression}, {@code className}, {@code fieldName}, {@code lineNumber}, {@code element}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + expression.hashCode();
    h += (h << 5) + className.hashCode();
    h += (h << 5) + fieldName.hashCode();
    h += (h << 5) + Long.hashCode(lineNumber);
    h += (h << 5) + element.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code FieldSourceLocation} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return "FieldSourceLocation{"
        + "expression=" + expression
        + ", className=" + className
        + ", fieldName=" + fieldName
        + ", lineNumber=" + lineNumber
        + ", element=" + element
        + "}";
  }

  /**
   * Creates an immutable copy of a {@link FieldSourceLocation} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable FieldSourceLocation instance
   */
  public static ImmutableFieldSourceLocation copyOf(FieldSourceLocation instance) {
    if (instance instanceof ImmutableFieldSourceLocation) {
      return (ImmutableFieldSourceLocation) instance;
    }
    return ImmutableFieldSourceLocation.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableFieldSourceLocation ImmutableFieldSourceLocation}.
   * <pre>
   * ImmutableFieldSourceLocation.builder()
   *    .expression(String) // required {@link FieldSourceLocation#getExpression() expression}
   *    .className(String) // required {@link FieldSourceLocation#getClassName() className}
   *    .fieldName(String) // required {@link FieldSourceLocation#getFieldName() fieldName}
   *    .lineNumber(long) // required {@link FieldSourceLocation#getLineNumber() lineNumber}
   *    .element(javax.lang.model.element.Element) // required {@link FieldSourceLocation#getElement() element}
   *    .build();
   * </pre>
   * @return A new ImmutableFieldSourceLocation builder
   */
  public static ImmutableFieldSourceLocation.Builder builder() {
    return new ImmutableFieldSourceLocation.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableFieldSourceLocation ImmutableFieldSourceLocation}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "FieldSourceLocation", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_EXPRESSION = 0x1L;
    private static final long INIT_BIT_CLASS_NAME = 0x2L;
    private static final long INIT_BIT_FIELD_NAME = 0x4L;
    private static final long INIT_BIT_LINE_NUMBER = 0x8L;
    private static final long INIT_BIT_ELEMENT = 0x10L;
    private long initBits = 0x1fL;

    private @Nullable String expression;
    private @Nullable String className;
    private @Nullable String fieldName;
    private long lineNumber;
    private @Nullable Element element;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code io.jpm.common.exception.cache.domain.vo.FieldSourceLocation} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(FieldSourceLocation instance) {
      Objects.requireNonNull(instance, "instance");
      from((short) 0, (Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code io.jpm.common.exception.cache.domain.vo.SourceLocation} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(SourceLocation instance) {
      Objects.requireNonNull(instance, "instance");
      from((short) 0, (Object) instance);
      return this;
    }

    private void from(short _unused, Object object) {
      if (object instanceof FieldSourceLocation) {
        FieldSourceLocation instance = (FieldSourceLocation) object;
        this.fieldName(instance.getFieldName());
      }
      if (object instanceof SourceLocation) {
        SourceLocation instance = (SourceLocation) object;
        this.className(instance.getClassName());
        this.expression(instance.getExpression());
        this.lineNumber(instance.getLineNumber());
        this.element(instance.getElement());
      }
    }

    /**
     * Initializes the value for the {@link FieldSourceLocation#getExpression() expression} attribute.
     * @param expression The value for expression 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder expression(String expression) {
      this.expression = Objects.requireNonNull(expression, "expression");
      initBits &= ~INIT_BIT_EXPRESSION;
      return this;
    }

    /**
     * Initializes the value for the {@link FieldSourceLocation#getClassName() className} attribute.
     * @param className The value for className 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder className(String className) {
      this.className = Objects.requireNonNull(className, "className");
      initBits &= ~INIT_BIT_CLASS_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link FieldSourceLocation#getFieldName() fieldName} attribute.
     * @param fieldName The value for fieldName 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder fieldName(String fieldName) {
      this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
      initBits &= ~INIT_BIT_FIELD_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link FieldSourceLocation#getLineNumber() lineNumber} attribute.
     * @param lineNumber The value for lineNumber 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder lineNumber(long lineNumber) {
      this.lineNumber = lineNumber;
      initBits &= ~INIT_BIT_LINE_NUMBER;
      return this;
    }

    /**
     * Initializes the value for the {@link FieldSourceLocation#getElement() element} attribute.
     * @param element The value for element 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder element(Element element) {
      this.element = Objects.requireNonNull(element, "element");
      initBits &= ~INIT_BIT_ELEMENT;
      return this;
    }

    /**
     * Builds a new {@link ImmutableFieldSourceLocation ImmutableFieldSourceLocation}.
     * @return An immutable instance of FieldSourceLocation
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableFieldSourceLocation build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableFieldSourceLocation(expression, className, fieldName, lineNumber, element);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_EXPRESSION) != 0) attributes.add("expression");
      if ((initBits & INIT_BIT_CLASS_NAME) != 0) attributes.add("className");
      if ((initBits & INIT_BIT_FIELD_NAME) != 0) attributes.add("fieldName");
      if ((initBits & INIT_BIT_LINE_NUMBER) != 0) attributes.add("lineNumber");
      if ((initBits & INIT_BIT_ELEMENT) != 0) attributes.add("element");
      return "Cannot build FieldSourceLocation, some of required attributes are not set " + attributes;
    }
  }
}
