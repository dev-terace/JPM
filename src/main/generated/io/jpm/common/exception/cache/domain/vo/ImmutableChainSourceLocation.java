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
 * Immutable implementation of {@link ChainSourceLocation}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableChainSourceLocation.builder()}.
 */
@Generated(from = "ChainSourceLocation", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
public final class ImmutableChainSourceLocation
    extends ChainSourceLocation {
  private final String className;
  private final long lineNumber;
  private final Element element;
  private final String expression;
  private final String methodName;
  private final String chainMethodName;
  private final int chainIndex;

  private ImmutableChainSourceLocation(
      String className,
      long lineNumber,
      Element element,
      String expression,
      String methodName,
      String chainMethodName,
      int chainIndex) {
    this.className = className;
    this.lineNumber = lineNumber;
    this.element = element;
    this.expression = expression;
    this.methodName = methodName;
    this.chainMethodName = chainMethodName;
    this.chainIndex = chainIndex;
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
   * @return The value of the {@code chainMethodName} attribute
   */
  @Override
  public String getChainMethodName() {
    return chainMethodName;
  }

  /**
   * @return The value of the {@code chainIndex} attribute
   */
  @Override
  public int getChainIndex() {
    return chainIndex;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getClassName() className} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for className
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withClassName(String value) {
    String newValue = Objects.requireNonNull(value, "className");
    if (this.className.equals(newValue)) return this;
    return new ImmutableChainSourceLocation(
        newValue,
        this.lineNumber,
        this.element,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.chainIndex);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getLineNumber() lineNumber} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for lineNumber
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withLineNumber(long value) {
    if (this.lineNumber == value) return this;
    return new ImmutableChainSourceLocation(
        this.className,
        value,
        this.element,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.chainIndex);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getElement() element} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for element
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withElement(Element value) {
    if (this.element == value) return this;
    Element newValue = Objects.requireNonNull(value, "element");
    return new ImmutableChainSourceLocation(
        this.className,
        this.lineNumber,
        newValue,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.chainIndex);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getExpression() expression} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for expression
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withExpression(String value) {
    String newValue = Objects.requireNonNull(value, "expression");
    if (this.expression.equals(newValue)) return this;
    return new ImmutableChainSourceLocation(
        this.className,
        this.lineNumber,
        this.element,
        newValue,
        this.methodName,
        this.chainMethodName,
        this.chainIndex);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getMethodName() methodName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for methodName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withMethodName(String value) {
    String newValue = Objects.requireNonNull(value, "methodName");
    if (this.methodName.equals(newValue)) return this;
    return new ImmutableChainSourceLocation(
        this.className,
        this.lineNumber,
        this.element,
        this.expression,
        newValue,
        this.chainMethodName,
        this.chainIndex);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getChainMethodName() chainMethodName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for chainMethodName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withChainMethodName(String value) {
    String newValue = Objects.requireNonNull(value, "chainMethodName");
    if (this.chainMethodName.equals(newValue)) return this;
    return new ImmutableChainSourceLocation(
        this.className,
        this.lineNumber,
        this.element,
        this.expression,
        this.methodName,
        newValue,
        this.chainIndex);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ChainSourceLocation#getChainIndex() chainIndex} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for chainIndex
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableChainSourceLocation withChainIndex(int value) {
    if (this.chainIndex == value) return this;
    return new ImmutableChainSourceLocation(
        this.className,
        this.lineNumber,
        this.element,
        this.expression,
        this.methodName,
        this.chainMethodName,
        value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableChainSourceLocation} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableChainSourceLocation
        && equalTo(0, (ImmutableChainSourceLocation) another);
  }

  private boolean equalTo(int synthetic, ImmutableChainSourceLocation another) {
    return className.equals(another.className)
        && lineNumber == another.lineNumber
        && element.equals(another.element)
        && expression.equals(another.expression)
        && methodName.equals(another.methodName)
        && chainMethodName.equals(another.chainMethodName)
        && chainIndex == another.chainIndex;
  }

  /**
   * Computes a hash code from attributes: {@code className}, {@code lineNumber}, {@code element}, {@code expression}, {@code methodName}, {@code chainMethodName}, {@code chainIndex}.
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
    h += (h << 5) + chainMethodName.hashCode();
    h += (h << 5) + chainIndex;
    return h;
  }

  /**
   * Prints the immutable value {@code ChainSourceLocation} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return "ChainSourceLocation{"
        + "className=" + className
        + ", lineNumber=" + lineNumber
        + ", element=" + element
        + ", expression=" + expression
        + ", methodName=" + methodName
        + ", chainMethodName=" + chainMethodName
        + ", chainIndex=" + chainIndex
        + "}";
  }

  /**
   * Creates an immutable copy of a {@link ChainSourceLocation} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ChainSourceLocation instance
   */
  public static ImmutableChainSourceLocation copyOf(ChainSourceLocation instance) {
    if (instance instanceof ImmutableChainSourceLocation) {
      return (ImmutableChainSourceLocation) instance;
    }
    return ImmutableChainSourceLocation.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableChainSourceLocation ImmutableChainSourceLocation}.
   * <pre>
   * ImmutableChainSourceLocation.builder()
   *    .className(String) // required {@link ChainSourceLocation#getClassName() className}
   *    .lineNumber(long) // required {@link ChainSourceLocation#getLineNumber() lineNumber}
   *    .element(javax.lang.model.element.Element) // required {@link ChainSourceLocation#getElement() element}
   *    .expression(String) // required {@link ChainSourceLocation#getExpression() expression}
   *    .methodName(String) // required {@link ChainSourceLocation#getMethodName() methodName}
   *    .chainMethodName(String) // required {@link ChainSourceLocation#getChainMethodName() chainMethodName}
   *    .chainIndex(int) // required {@link ChainSourceLocation#getChainIndex() chainIndex}
   *    .build();
   * </pre>
   * @return A new ImmutableChainSourceLocation builder
   */
  public static ImmutableChainSourceLocation.Builder builder() {
    return new ImmutableChainSourceLocation.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableChainSourceLocation ImmutableChainSourceLocation}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ChainSourceLocation", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_CLASS_NAME = 0x1L;
    private static final long INIT_BIT_LINE_NUMBER = 0x2L;
    private static final long INIT_BIT_ELEMENT = 0x4L;
    private static final long INIT_BIT_EXPRESSION = 0x8L;
    private static final long INIT_BIT_METHOD_NAME = 0x10L;
    private static final long INIT_BIT_CHAIN_METHOD_NAME = 0x20L;
    private static final long INIT_BIT_CHAIN_INDEX = 0x40L;
    private long initBits = 0x7fL;

    private @Nullable String className;
    private long lineNumber;
    private @Nullable Element element;
    private @Nullable String expression;
    private @Nullable String methodName;
    private @Nullable String chainMethodName;
    private int chainIndex;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code io.jpm.common.exception.cache.domain.vo.ChainSourceLocation} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(ChainSourceLocation instance) {
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
      if (object instanceof ChainSourceLocation) {
        ChainSourceLocation instance = (ChainSourceLocation) object;
        this.chainIndex(instance.getChainIndex());
        this.methodName(instance.getMethodName());
        this.chainMethodName(instance.getChainMethodName());
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
     * Initializes the value for the {@link ChainSourceLocation#getClassName() className} attribute.
     * @param className The value for className 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder className(String className) {
      this.className = Objects.requireNonNull(className, "className");
      initBits &= ~INIT_BIT_CLASS_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link ChainSourceLocation#getLineNumber() lineNumber} attribute.
     * @param lineNumber The value for lineNumber 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder lineNumber(long lineNumber) {
      this.lineNumber = lineNumber;
      initBits &= ~INIT_BIT_LINE_NUMBER;
      return this;
    }

    /**
     * Initializes the value for the {@link ChainSourceLocation#getElement() element} attribute.
     * @param element The value for element 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder element(Element element) {
      this.element = Objects.requireNonNull(element, "element");
      initBits &= ~INIT_BIT_ELEMENT;
      return this;
    }

    /**
     * Initializes the value for the {@link ChainSourceLocation#getExpression() expression} attribute.
     * @param expression The value for expression 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder expression(String expression) {
      this.expression = Objects.requireNonNull(expression, "expression");
      initBits &= ~INIT_BIT_EXPRESSION;
      return this;
    }

    /**
     * Initializes the value for the {@link ChainSourceLocation#getMethodName() methodName} attribute.
     * @param methodName The value for methodName 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder methodName(String methodName) {
      this.methodName = Objects.requireNonNull(methodName, "methodName");
      initBits &= ~INIT_BIT_METHOD_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link ChainSourceLocation#getChainMethodName() chainMethodName} attribute.
     * @param chainMethodName The value for chainMethodName 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder chainMethodName(String chainMethodName) {
      this.chainMethodName = Objects.requireNonNull(chainMethodName, "chainMethodName");
      initBits &= ~INIT_BIT_CHAIN_METHOD_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link ChainSourceLocation#getChainIndex() chainIndex} attribute.
     * @param chainIndex The value for chainIndex 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder chainIndex(int chainIndex) {
      this.chainIndex = chainIndex;
      initBits &= ~INIT_BIT_CHAIN_INDEX;
      return this;
    }

    /**
     * Builds a new {@link ImmutableChainSourceLocation ImmutableChainSourceLocation}.
     * @return An immutable instance of ChainSourceLocation
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableChainSourceLocation build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableChainSourceLocation(className, lineNumber, element, expression, methodName, chainMethodName, chainIndex);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_CLASS_NAME) != 0) attributes.add("className");
      if ((initBits & INIT_BIT_LINE_NUMBER) != 0) attributes.add("lineNumber");
      if ((initBits & INIT_BIT_ELEMENT) != 0) attributes.add("element");
      if ((initBits & INIT_BIT_EXPRESSION) != 0) attributes.add("expression");
      if ((initBits & INIT_BIT_METHOD_NAME) != 0) attributes.add("methodName");
      if ((initBits & INIT_BIT_CHAIN_METHOD_NAME) != 0) attributes.add("chainMethodName");
      if ((initBits & INIT_BIT_CHAIN_INDEX) != 0) attributes.add("chainIndex");
      return "Cannot build ChainSourceLocation, some of required attributes are not set " + attributes;
    }
  }
}
