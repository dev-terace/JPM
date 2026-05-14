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
 * Immutable implementation of {@link MethodSourceLocation}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableMethodSourceLocation.builder()}.
 */
@Generated(from = "MethodSourceLocation", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
public final class ImmutableMethodSourceLocation
    extends MethodSourceLocation {
  private final String className;
  private final long lineNumber;
  private final Element element;
  private final String expression;
  private final String methodName;
  private final AnnotationType type;

  private ImmutableMethodSourceLocation(
      String className,
      long lineNumber,
      Element element,
      String expression,
      String methodName,
      AnnotationType type) {
    this.className = className;
    this.lineNumber = lineNumber;
    this.element = element;
    this.expression = expression;
    this.methodName = methodName;
    this.type = type;
  }

  /**
   * @return The value of the {@code className} attribute
   */
  @Override
  public String getClassName() {
    return className;
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
   * @return The value of the {@code expression} attribute
   */
  @Override
  public String getExpression() {
    return expression;
  }

  /**
   * @return The value of the {@code methodName} attribute
   */
  @Override
  public String getMethodName() {
    return methodName;
  }

  /**
   * @return The value of the {@code type} attribute
   */
  @Override
  public AnnotationType getType() {
    return type;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MethodSourceLocation#getClassName() className} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for className
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableMethodSourceLocation withClassName(String value) {
    String newValue = Objects.requireNonNull(value, "className");
    if (this.className.equals(newValue)) return this;
    return new ImmutableMethodSourceLocation(newValue, this.lineNumber, this.element, this.expression, this.methodName, this.type);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MethodSourceLocation#getLineNumber() lineNumber} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for lineNumber
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableMethodSourceLocation withLineNumber(long value) {
    if (this.lineNumber == value) return this;
    return new ImmutableMethodSourceLocation(this.className, value, this.element, this.expression, this.methodName, this.type);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MethodSourceLocation#getElement() element} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for element
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableMethodSourceLocation withElement(Element value) {
    if (this.element == value) return this;
    Element newValue = Objects.requireNonNull(value, "element");
    return new ImmutableMethodSourceLocation(this.className, this.lineNumber, newValue, this.expression, this.methodName, this.type);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MethodSourceLocation#getExpression() expression} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for expression
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableMethodSourceLocation withExpression(String value) {
    String newValue = Objects.requireNonNull(value, "expression");
    if (this.expression.equals(newValue)) return this;
    return new ImmutableMethodSourceLocation(this.className, this.lineNumber, this.element, newValue, this.methodName, this.type);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MethodSourceLocation#getMethodName() methodName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for methodName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableMethodSourceLocation withMethodName(String value) {
    String newValue = Objects.requireNonNull(value, "methodName");
    if (this.methodName.equals(newValue)) return this;
    return new ImmutableMethodSourceLocation(this.className, this.lineNumber, this.element, this.expression, newValue, this.type);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link MethodSourceLocation#getType() type} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableMethodSourceLocation withType(AnnotationType value) {
    AnnotationType newValue = Objects.requireNonNull(value, "type");
    if (this.type == newValue) return this;
    return new ImmutableMethodSourceLocation(this.className, this.lineNumber, this.element, this.expression, this.methodName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableMethodSourceLocation} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableMethodSourceLocation
        && equalTo(0, (ImmutableMethodSourceLocation) another);
  }

  private boolean equalTo(int synthetic, ImmutableMethodSourceLocation another) {
    return className.equals(another.className)
        && lineNumber == another.lineNumber
        && element.equals(another.element)
        && expression.equals(another.expression)
        && methodName.equals(another.methodName)
        && type.equals(another.type);
  }

  /**
   * Computes a hash code from attributes: {@code className}, {@code lineNumber}, {@code element}, {@code expression}, {@code methodName}, {@code type}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + className.hashCode();
    h += (h << 5) + Long.hashCode(lineNumber);
    h += (h << 5) + element.hashCode();
    h += (h << 5) + expression.hashCode();
    h += (h << 5) + methodName.hashCode();
    h += (h << 5) + type.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code MethodSourceLocation} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return "MethodSourceLocation{"
        + "className=" + className
        + ", lineNumber=" + lineNumber
        + ", element=" + element
        + ", expression=" + expression
        + ", methodName=" + methodName
        + ", type=" + type
        + "}";
  }

  /**
   * Creates an immutable copy of a {@link MethodSourceLocation} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable MethodSourceLocation instance
   */
  public static ImmutableMethodSourceLocation copyOf(MethodSourceLocation instance) {
    if (instance instanceof ImmutableMethodSourceLocation) {
      return (ImmutableMethodSourceLocation) instance;
    }
    return ImmutableMethodSourceLocation.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableMethodSourceLocation ImmutableMethodSourceLocation}.
   * <pre>
   * ImmutableMethodSourceLocation.builder()
   *    .className(String) // required {@link MethodSourceLocation#getClassName() className}
   *    .lineNumber(long) // required {@link MethodSourceLocation#getLineNumber() lineNumber}
   *    .element(javax.lang.model.element.Element) // required {@link MethodSourceLocation#getElement() element}
   *    .expression(String) // required {@link MethodSourceLocation#getExpression() expression}
   *    .methodName(String) // required {@link MethodSourceLocation#getMethodName() methodName}
   *    .type(io.jpm.common.exception.cache.domain.vo.AnnotationType) // required {@link MethodSourceLocation#getType() type}
   *    .build();
   * </pre>
   * @return A new ImmutableMethodSourceLocation builder
   */
  public static ImmutableMethodSourceLocation.Builder builder() {
    return new ImmutableMethodSourceLocation.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableMethodSourceLocation ImmutableMethodSourceLocation}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "MethodSourceLocation", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_CLASS_NAME = 0x1L;
    private static final long INIT_BIT_LINE_NUMBER = 0x2L;
    private static final long INIT_BIT_ELEMENT = 0x4L;
    private static final long INIT_BIT_EXPRESSION = 0x8L;
    private static final long INIT_BIT_METHOD_NAME = 0x10L;
    private static final long INIT_BIT_TYPE = 0x20L;
    private long initBits = 0x3fL;

    private @Nullable String className;
    private long lineNumber;
    private @Nullable Element element;
    private @Nullable String expression;
    private @Nullable String methodName;
    private @Nullable AnnotationType type;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code io.jpm.common.exception.cache.domain.vo.MethodSourceLocation} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(MethodSourceLocation instance) {
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
      if (object instanceof MethodSourceLocation) {
        MethodSourceLocation instance = (MethodSourceLocation) object;
        this.type(instance.getType());
        this.methodName(instance.getMethodName());
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
     * Initializes the value for the {@link MethodSourceLocation#getClassName() className} attribute.
     * @param className The value for className 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder className(String className) {
      this.className = Objects.requireNonNull(className, "className");
      initBits &= ~INIT_BIT_CLASS_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link MethodSourceLocation#getLineNumber() lineNumber} attribute.
     * @param lineNumber The value for lineNumber 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder lineNumber(long lineNumber) {
      this.lineNumber = lineNumber;
      initBits &= ~INIT_BIT_LINE_NUMBER;
      return this;
    }

    /**
     * Initializes the value for the {@link MethodSourceLocation#getElement() element} attribute.
     * @param element The value for element 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder element(Element element) {
      this.element = Objects.requireNonNull(element, "element");
      initBits &= ~INIT_BIT_ELEMENT;
      return this;
    }

    /**
     * Initializes the value for the {@link MethodSourceLocation#getExpression() expression} attribute.
     * @param expression The value for expression 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder expression(String expression) {
      this.expression = Objects.requireNonNull(expression, "expression");
      initBits &= ~INIT_BIT_EXPRESSION;
      return this;
    }

    /**
     * Initializes the value for the {@link MethodSourceLocation#getMethodName() methodName} attribute.
     * @param methodName The value for methodName 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder methodName(String methodName) {
      this.methodName = Objects.requireNonNull(methodName, "methodName");
      initBits &= ~INIT_BIT_METHOD_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link MethodSourceLocation#getType() type} attribute.
     * @param type The value for type 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder type(AnnotationType type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }

    /**
     * Builds a new {@link ImmutableMethodSourceLocation ImmutableMethodSourceLocation}.
     * @return An immutable instance of MethodSourceLocation
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableMethodSourceLocation build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableMethodSourceLocation(className, lineNumber, element, expression, methodName, type);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_CLASS_NAME) != 0) attributes.add("className");
      if ((initBits & INIT_BIT_LINE_NUMBER) != 0) attributes.add("lineNumber");
      if ((initBits & INIT_BIT_ELEMENT) != 0) attributes.add("element");
      if ((initBits & INIT_BIT_EXPRESSION) != 0) attributes.add("expression");
      if ((initBits & INIT_BIT_METHOD_NAME) != 0) attributes.add("methodName");
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      return "Cannot build MethodSourceLocation, some of required attributes are not set " + attributes;
    }
  }
}
