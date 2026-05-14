package io.jpm.common.exception;

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
 * Immutable implementation of {@link ErrorInfo}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableErrorInfo.builder()}.
 */
@Generated(from = "ErrorInfo", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
public final class ImmutableErrorInfo extends ErrorInfo {
  private final ErrorCode err;
  private final String className;
  private final @Nullable String expression;
  private final @Nullable String methodName;
  private final @Nullable String chainMethodName;
  private final @Nullable String fieldName;
  private final @Nullable Element errorElement;
  private final @Nullable Integer lineNumber;

  private ImmutableErrorInfo(
      ErrorCode err,
      String className,
      @Nullable String expression,
      @Nullable String methodName,
      @Nullable String chainMethodName,
      @Nullable String fieldName,
      @Nullable Element errorElement,
      @Nullable Integer lineNumber) {
    this.err = err;
    this.className = className;
    this.expression = expression;
    this.methodName = methodName;
    this.chainMethodName = chainMethodName;
    this.fieldName = fieldName;
    this.errorElement = errorElement;
    this.lineNumber = lineNumber;
  }

  /**
   * @return The value of the {@code err} attribute
   */
  @Override
  public ErrorCode getErr() {
    return err;
  }

  /**
   * @return The value of the {@code className} attribute
   */
  @Override
  public String getClassName() {
    return className;
  }

  /**
   * @return The value of the {@code expression} attribute
   */
  @Override
  public @Nullable String getExpression() {
    return expression;
  }

  /**
   * @return The value of the {@code methodName} attribute
   */
  @Override
  public @Nullable String getMethodName() {
    return methodName;
  }

  /**
   * @return The value of the {@code chainMethodName} attribute
   */
  @Override
  public @Nullable String getChainMethodName() {
    return chainMethodName;
  }

  /**
   * @return The value of the {@code fieldName} attribute
   */
  @Override
  public @Nullable String getFieldName() {
    return fieldName;
  }

  /**
   * @return The value of the {@code errorElement} attribute
   */
  @Override
  public @Nullable Element getErrorElement() {
    return errorElement;
  }

  /**
   * @return The value of the {@code lineNumber} attribute
   */
  @Override
  public @Nullable Integer getLineNumber() {
    return lineNumber;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getErr() err} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for err
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withErr(ErrorCode value) {
    ErrorCode newValue = Objects.requireNonNull(value, "err");
    if (this.err == newValue) return this;
    return new ImmutableErrorInfo(
        newValue,
        this.className,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.fieldName,
        this.errorElement,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getClassName() className} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for className
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withClassName(String value) {
    String newValue = Objects.requireNonNull(value, "className");
    if (this.className.equals(newValue)) return this;
    return new ImmutableErrorInfo(
        this.err,
        newValue,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.fieldName,
        this.errorElement,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getExpression() expression} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for expression (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withExpression(@Nullable String value) {
    if (Objects.equals(this.expression, value)) return this;
    return new ImmutableErrorInfo(
        this.err,
        this.className,
        value,
        this.methodName,
        this.chainMethodName,
        this.fieldName,
        this.errorElement,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getMethodName() methodName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for methodName (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withMethodName(@Nullable String value) {
    if (Objects.equals(this.methodName, value)) return this;
    return new ImmutableErrorInfo(
        this.err,
        this.className,
        this.expression,
        value,
        this.chainMethodName,
        this.fieldName,
        this.errorElement,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getChainMethodName() chainMethodName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for chainMethodName (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withChainMethodName(@Nullable String value) {
    if (Objects.equals(this.chainMethodName, value)) return this;
    return new ImmutableErrorInfo(
        this.err,
        this.className,
        this.expression,
        this.methodName,
        value,
        this.fieldName,
        this.errorElement,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getFieldName() fieldName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for fieldName (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withFieldName(@Nullable String value) {
    if (Objects.equals(this.fieldName, value)) return this;
    return new ImmutableErrorInfo(
        this.err,
        this.className,
        this.expression,
        this.methodName,
        this.chainMethodName,
        value,
        this.errorElement,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getErrorElement() errorElement} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for errorElement (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withErrorElement(@Nullable Element value) {
    if (this.errorElement == value) return this;
    return new ImmutableErrorInfo(
        this.err,
        this.className,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.fieldName,
        value,
        this.lineNumber);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ErrorInfo#getLineNumber() lineNumber} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for lineNumber (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableErrorInfo withLineNumber(@Nullable Integer value) {
    if (Objects.equals(this.lineNumber, value)) return this;
    return new ImmutableErrorInfo(
        this.err,
        this.className,
        this.expression,
        this.methodName,
        this.chainMethodName,
        this.fieldName,
        this.errorElement,
        value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableErrorInfo} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableErrorInfo
        && equalTo(0, (ImmutableErrorInfo) another);
  }

  private boolean equalTo(int synthetic, ImmutableErrorInfo another) {
    return err.equals(another.err)
        && className.equals(another.className)
        && Objects.equals(expression, another.expression)
        && Objects.equals(methodName, another.methodName)
        && Objects.equals(chainMethodName, another.chainMethodName)
        && Objects.equals(fieldName, another.fieldName)
        && Objects.equals(errorElement, another.errorElement)
        && Objects.equals(lineNumber, another.lineNumber);
  }

  /**
   * Computes a hash code from attributes: {@code err}, {@code className}, {@code expression}, {@code methodName}, {@code chainMethodName}, {@code fieldName}, {@code errorElement}, {@code lineNumber}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + err.hashCode();
    h += (h << 5) + className.hashCode();
    h += (h << 5) + Objects.hashCode(expression);
    h += (h << 5) + Objects.hashCode(methodName);
    h += (h << 5) + Objects.hashCode(chainMethodName);
    h += (h << 5) + Objects.hashCode(fieldName);
    h += (h << 5) + Objects.hashCode(errorElement);
    h += (h << 5) + Objects.hashCode(lineNumber);
    return h;
  }

  /**
   * Creates an immutable copy of a {@link ErrorInfo} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ErrorInfo instance
   */
  public static ImmutableErrorInfo copyOf(ErrorInfo instance) {
    if (instance instanceof ImmutableErrorInfo) {
      return (ImmutableErrorInfo) instance;
    }
    return ImmutableErrorInfo.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableErrorInfo ImmutableErrorInfo}.
   * <pre>
   * ImmutableErrorInfo.builder()
   *    .err(io.jpm.common.exception.ErrorCode) // required {@link ErrorInfo#getErr() err}
   *    .className(String) // required {@link ErrorInfo#getClassName() className}
   *    .expression(String | null) // nullable {@link ErrorInfo#getExpression() expression}
   *    .methodName(String | null) // nullable {@link ErrorInfo#getMethodName() methodName}
   *    .chainMethodName(String | null) // nullable {@link ErrorInfo#getChainMethodName() chainMethodName}
   *    .fieldName(String | null) // nullable {@link ErrorInfo#getFieldName() fieldName}
   *    .errorElement(javax.lang.model.element.Element | null) // nullable {@link ErrorInfo#getErrorElement() errorElement}
   *    .lineNumber(Integer | null) // nullable {@link ErrorInfo#getLineNumber() lineNumber}
   *    .build();
   * </pre>
   * @return A new ImmutableErrorInfo builder
   */
  public static ImmutableErrorInfo.Builder builder() {
    return new ImmutableErrorInfo.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableErrorInfo ImmutableErrorInfo}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ErrorInfo", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_ERR = 0x1L;
    private static final long INIT_BIT_CLASS_NAME = 0x2L;
    private long initBits = 0x3L;

    private @Nullable ErrorCode err;
    private @Nullable String className;
    private @Nullable String expression;
    private @Nullable String methodName;
    private @Nullable String chainMethodName;
    private @Nullable String fieldName;
    private @Nullable Element errorElement;
    private @Nullable Integer lineNumber;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ErrorInfo} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(ErrorInfo instance) {
      Objects.requireNonNull(instance, "instance");
      this.err(instance.getErr());
      this.className(instance.getClassName());
      @Nullable String expressionValue = instance.getExpression();
      if (expressionValue != null) {
        expression(expressionValue);
      }
      @Nullable String methodNameValue = instance.getMethodName();
      if (methodNameValue != null) {
        methodName(methodNameValue);
      }
      @Nullable String chainMethodNameValue = instance.getChainMethodName();
      if (chainMethodNameValue != null) {
        chainMethodName(chainMethodNameValue);
      }
      @Nullable String fieldNameValue = instance.getFieldName();
      if (fieldNameValue != null) {
        fieldName(fieldNameValue);
      }
      @Nullable Element errorElementValue = instance.getErrorElement();
      if (errorElementValue != null) {
        errorElement(errorElementValue);
      }
      @Nullable Integer lineNumberValue = instance.getLineNumber();
      if (lineNumberValue != null) {
        lineNumber(lineNumberValue);
      }
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getErr() err} attribute.
     * @param err The value for err 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder err(ErrorCode err) {
      this.err = Objects.requireNonNull(err, "err");
      initBits &= ~INIT_BIT_ERR;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getClassName() className} attribute.
     * @param className The value for className 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder className(String className) {
      this.className = Objects.requireNonNull(className, "className");
      initBits &= ~INIT_BIT_CLASS_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getExpression() expression} attribute.
     * @param expression The value for expression (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder expression(@Nullable String expression) {
      this.expression = expression;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getMethodName() methodName} attribute.
     * @param methodName The value for methodName (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder methodName(@Nullable String methodName) {
      this.methodName = methodName;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getChainMethodName() chainMethodName} attribute.
     * @param chainMethodName The value for chainMethodName (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder chainMethodName(@Nullable String chainMethodName) {
      this.chainMethodName = chainMethodName;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getFieldName() fieldName} attribute.
     * @param fieldName The value for fieldName (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder fieldName(@Nullable String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getErrorElement() errorElement} attribute.
     * @param errorElement The value for errorElement (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder errorElement(@Nullable Element errorElement) {
      this.errorElement = errorElement;
      return this;
    }

    /**
     * Initializes the value for the {@link ErrorInfo#getLineNumber() lineNumber} attribute.
     * @param lineNumber The value for lineNumber (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder lineNumber(@Nullable Integer lineNumber) {
      this.lineNumber = lineNumber;
      return this;
    }

    /**
     * Builds a new {@link ImmutableErrorInfo ImmutableErrorInfo}.
     * @return An immutable instance of ErrorInfo
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableErrorInfo build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableErrorInfo(err, className, expression, methodName, chainMethodName, fieldName, errorElement, lineNumber);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_ERR) != 0) attributes.add("err");
      if ((initBits & INIT_BIT_CLASS_NAME) != 0) attributes.add("className");
      return "Cannot build ErrorInfo, some of required attributes are not set " + attributes;
    }
  }
}
