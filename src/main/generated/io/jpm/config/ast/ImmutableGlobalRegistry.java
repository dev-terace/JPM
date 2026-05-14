package io.jpm.config.ast;

import io.jpm.common.exception.ErrorTracker;
import io.jpm.config.AutoDDLPolicy;
import io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core.DslCommandProcStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core.DslCommandProcessorValidStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step.SegmentInlinerStep;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor;
import io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.LocalVariableCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link GlobalRegistry}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableGlobalRegistry.builder()}.
 */
@Generated(from = "GlobalRegistry", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
public final class ImmutableGlobalRegistry extends GlobalRegistry {
  private final Map<String, String> options;
  private transient final AutoDDLPolicy autoPolicy;
  private transient final String dbType;
  private final ArgumentTokenExtractor tokenExtractor;
  private final DslCommandProcStep commandProcessor;
  private final MapParamRegistryImpl mapParamRegistry;
  private final SegmentInlinerStep segmentInliner;
  private final ErrorTracker errorTracker;
  private final DslCommandProcessorValidStep findRepoMetaValidProc;
  private final LocalVariableCollector localVariableCollector;

  private ImmutableGlobalRegistry(
      Map<String, String> options,
      ArgumentTokenExtractor tokenExtractor,
      DslCommandProcStep commandProcessor,
      MapParamRegistryImpl mapParamRegistry,
      SegmentInlinerStep segmentInliner,
      ErrorTracker errorTracker,
      DslCommandProcessorValidStep findRepoMetaValidProc,
      LocalVariableCollector localVariableCollector) {
    this.options = options;
    this.tokenExtractor = tokenExtractor;
    this.commandProcessor = commandProcessor;
    this.mapParamRegistry = mapParamRegistry;
    this.segmentInliner = segmentInliner;
    this.errorTracker = errorTracker;
    this.findRepoMetaValidProc = findRepoMetaValidProc;
    this.localVariableCollector = localVariableCollector;
    this.autoPolicy = initShim.autoPolicy();
    this.dbType = initShim.dbType();
    this.initShim = null;
  }

  private static final byte STAGE_INITIALIZING = -1;
  private static final byte STAGE_UNINITIALIZED = 0;
  private static final byte STAGE_INITIALIZED = 1;
  private transient volatile InitShim initShim = new InitShim();

  @Generated(from = "GlobalRegistry", generator = "Immutables")
  private final class InitShim {
    private byte autoPolicyBuildStage = STAGE_UNINITIALIZED;
    private AutoDDLPolicy autoPolicy;

    AutoDDLPolicy autoPolicy() {
      if (autoPolicyBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (autoPolicyBuildStage == STAGE_UNINITIALIZED) {
        autoPolicyBuildStage = STAGE_INITIALIZING;
        this.autoPolicy = Objects.requireNonNull(ImmutableGlobalRegistry.super.autoPolicy(), "autoPolicy");
        autoPolicyBuildStage = STAGE_INITIALIZED;
      }
      return this.autoPolicy;
    }

    private byte dbTypeBuildStage = STAGE_UNINITIALIZED;
    private String dbType;

    String dbType() {
      if (dbTypeBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
      if (dbTypeBuildStage == STAGE_UNINITIALIZED) {
        dbTypeBuildStage = STAGE_INITIALIZING;
        this.dbType = Objects.requireNonNull(ImmutableGlobalRegistry.super.dbType(), "dbType");
        dbTypeBuildStage = STAGE_INITIALIZED;
      }
      return this.dbType;
    }

    private String formatInitCycleMessage() {
      List<String> attributes = new ArrayList<>();
      if (autoPolicyBuildStage == STAGE_INITIALIZING) attributes.add("autoPolicy");
      if (dbTypeBuildStage == STAGE_INITIALIZING) attributes.add("dbType");
      return "Cannot build GlobalRegistry, attribute initializers form cycle " + attributes;
    }
  }

  /**
   * @return The value of the {@code options} attribute
   */
  @Override
  public Map<String, String> options() {
    return options;
  }

  /**
   * @return The computed-at-construction value of the {@code autoPolicy} attribute
   */
  @Override
  public AutoDDLPolicy autoPolicy() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.autoPolicy()
        : this.autoPolicy;
  }

  /**
   * @return The computed-at-construction value of the {@code dbType} attribute
   */
  @Override
  public String dbType() {
    InitShim shim = this.initShim;
    return shim != null
        ? shim.dbType()
        : this.dbType;
  }

  /**
   * @return The value of the {@code tokenExtractor} attribute
   */
  @Override
  public ArgumentTokenExtractor tokenExtractor() {
    return tokenExtractor;
  }

  /**
   * @return The value of the {@code commandProcessor} attribute
   */
  @Override
  public DslCommandProcStep commandProcessor() {
    return commandProcessor;
  }

  /**
   * @return The value of the {@code mapParamRegistry} attribute
   */
  @Override
  public MapParamRegistryImpl mapParamRegistry() {
    return mapParamRegistry;
  }

  /**
   * @return The value of the {@code segmentInliner} attribute
   */
  @Override
  public SegmentInlinerStep segmentInliner() {
    return segmentInliner;
  }

  /**
   * @return The value of the {@code errorTracker} attribute
   */
  @Override
  public ErrorTracker errorTracker() {
    return errorTracker;
  }

  /**
   * @return The value of the {@code findRepoMetaValidProc} attribute
   */
  @Override
  public DslCommandProcessorValidStep findRepoMetaValidProc() {
    return findRepoMetaValidProc;
  }

  /**
   * @return The value of the {@code localVariableCollector} attribute
   */
  @Override
  public LocalVariableCollector localVariableCollector() {
    return localVariableCollector;
  }

  /**
   * Copy the current immutable object by replacing the {@link GlobalRegistry#options() options} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the options map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableGlobalRegistry withOptions(Map<String, ? extends String> entries) {
    if (this.options == entries) return this;
    Map<String, String> newValue = createUnmodifiableMap(true, false, entries);
    return new ImmutableGlobalRegistry(
        newValue,
        this.tokenExtractor,
        this.commandProcessor,
        this.mapParamRegistry,
        this.segmentInliner,
        this.errorTracker,
        this.findRepoMetaValidProc,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#tokenExtractor() tokenExtractor} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for tokenExtractor
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withTokenExtractor(ArgumentTokenExtractor value) {
    if (this.tokenExtractor == value) return this;
    ArgumentTokenExtractor newValue = Objects.requireNonNull(value, "tokenExtractor");
    return new ImmutableGlobalRegistry(
        this.options,
        newValue,
        this.commandProcessor,
        this.mapParamRegistry,
        this.segmentInliner,
        this.errorTracker,
        this.findRepoMetaValidProc,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#commandProcessor() commandProcessor} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for commandProcessor
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withCommandProcessor(DslCommandProcStep value) {
    if (this.commandProcessor == value) return this;
    DslCommandProcStep newValue = Objects.requireNonNull(value, "commandProcessor");
    return new ImmutableGlobalRegistry(
        this.options,
        this.tokenExtractor,
        newValue,
        this.mapParamRegistry,
        this.segmentInliner,
        this.errorTracker,
        this.findRepoMetaValidProc,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#mapParamRegistry() mapParamRegistry} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for mapParamRegistry
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withMapParamRegistry(MapParamRegistryImpl value) {
    if (this.mapParamRegistry == value) return this;
    MapParamRegistryImpl newValue = Objects.requireNonNull(value, "mapParamRegistry");
    return new ImmutableGlobalRegistry(
        this.options,
        this.tokenExtractor,
        this.commandProcessor,
        newValue,
        this.segmentInliner,
        this.errorTracker,
        this.findRepoMetaValidProc,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#segmentInliner() segmentInliner} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for segmentInliner
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withSegmentInliner(SegmentInlinerStep value) {
    if (this.segmentInliner == value) return this;
    SegmentInlinerStep newValue = Objects.requireNonNull(value, "segmentInliner");
    return new ImmutableGlobalRegistry(
        this.options,
        this.tokenExtractor,
        this.commandProcessor,
        this.mapParamRegistry,
        newValue,
        this.errorTracker,
        this.findRepoMetaValidProc,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#errorTracker() errorTracker} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for errorTracker
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withErrorTracker(ErrorTracker value) {
    if (this.errorTracker == value) return this;
    ErrorTracker newValue = Objects.requireNonNull(value, "errorTracker");
    return new ImmutableGlobalRegistry(
        this.options,
        this.tokenExtractor,
        this.commandProcessor,
        this.mapParamRegistry,
        this.segmentInliner,
        newValue,
        this.findRepoMetaValidProc,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#findRepoMetaValidProc() findRepoMetaValidProc} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for findRepoMetaValidProc
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withFindRepoMetaValidProc(DslCommandProcessorValidStep value) {
    if (this.findRepoMetaValidProc == value) return this;
    DslCommandProcessorValidStep newValue = Objects.requireNonNull(value, "findRepoMetaValidProc");
    return new ImmutableGlobalRegistry(
        this.options,
        this.tokenExtractor,
        this.commandProcessor,
        this.mapParamRegistry,
        this.segmentInliner,
        this.errorTracker,
        newValue,
        this.localVariableCollector);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GlobalRegistry#localVariableCollector() localVariableCollector} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for localVariableCollector
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGlobalRegistry withLocalVariableCollector(LocalVariableCollector value) {
    if (this.localVariableCollector == value) return this;
    LocalVariableCollector newValue = Objects.requireNonNull(value, "localVariableCollector");
    return new ImmutableGlobalRegistry(
        this.options,
        this.tokenExtractor,
        this.commandProcessor,
        this.mapParamRegistry,
        this.segmentInliner,
        this.errorTracker,
        this.findRepoMetaValidProc,
        newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableGlobalRegistry} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableGlobalRegistry
        && equalTo(0, (ImmutableGlobalRegistry) another);
  }

  private boolean equalTo(int synthetic, ImmutableGlobalRegistry another) {
    return options.equals(another.options)
        && autoPolicy.equals(another.autoPolicy)
        && dbType.equals(another.dbType)
        && tokenExtractor.equals(another.tokenExtractor)
        && commandProcessor.equals(another.commandProcessor)
        && mapParamRegistry.equals(another.mapParamRegistry)
        && segmentInliner.equals(another.segmentInliner)
        && errorTracker.equals(another.errorTracker)
        && findRepoMetaValidProc.equals(another.findRepoMetaValidProc)
        && localVariableCollector.equals(another.localVariableCollector);
  }

  /**
   * Computes a hash code from attributes: {@code options}, {@code autoPolicy}, {@code dbType}, {@code tokenExtractor}, {@code commandProcessor}, {@code mapParamRegistry}, {@code segmentInliner}, {@code errorTracker}, {@code findRepoMetaValidProc}, {@code localVariableCollector}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 5381;
    h += (h << 5) + options.hashCode();
    h += (h << 5) + autoPolicy.hashCode();
    h += (h << 5) + dbType.hashCode();
    h += (h << 5) + tokenExtractor.hashCode();
    h += (h << 5) + commandProcessor.hashCode();
    h += (h << 5) + mapParamRegistry.hashCode();
    h += (h << 5) + segmentInliner.hashCode();
    h += (h << 5) + errorTracker.hashCode();
    h += (h << 5) + findRepoMetaValidProc.hashCode();
    h += (h << 5) + localVariableCollector.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code GlobalRegistry} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return "GlobalRegistry{"
        + "options=" + options
        + ", autoPolicy=" + autoPolicy
        + ", dbType=" + dbType
        + ", tokenExtractor=" + tokenExtractor
        + ", commandProcessor=" + commandProcessor
        + ", mapParamRegistry=" + mapParamRegistry
        + ", segmentInliner=" + segmentInliner
        + ", errorTracker=" + errorTracker
        + ", findRepoMetaValidProc=" + findRepoMetaValidProc
        + ", localVariableCollector=" + localVariableCollector
        + "}";
  }

  /**
   * Creates an immutable copy of a {@link GlobalRegistry} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable GlobalRegistry instance
   */
  public static ImmutableGlobalRegistry copyOf(GlobalRegistry instance) {
    if (instance instanceof ImmutableGlobalRegistry) {
      return (ImmutableGlobalRegistry) instance;
    }
    return ImmutableGlobalRegistry.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableGlobalRegistry ImmutableGlobalRegistry}.
   * <pre>
   * ImmutableGlobalRegistry.builder()
   *    .putOptions|putAllOptions(String =&gt; String) // {@link GlobalRegistry#options() options} mappings
   *    .tokenExtractor(io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.support.ArgumentTokenExtractor) // required {@link GlobalRegistry#tokenExtractor() tokenExtractor}
   *    .commandProcessor(io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core.DslCommandProcStep) // required {@link GlobalRegistry#commandProcessor() commandProcessor}
   *    .mapParamRegistry(io.jpm.core.jpm_repository.domain.cache.MapParamRegistryImpl) // required {@link GlobalRegistry#mapParamRegistry() mapParamRegistry}
   *    .segmentInliner(io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.parse_method_body_step.SegmentInlinerStep) // required {@link GlobalRegistry#segmentInliner() segmentInliner}
   *    .errorTracker(io.jpm.common.exception.ErrorTracker) // required {@link GlobalRegistry#errorTracker() errorTracker}
   *    .findRepoMetaValidProc(io.jpm.core.jpm_repository.steps.find_repo_meta_handler.core.DslCommandProcessorValidStep) // required {@link GlobalRegistry#findRepoMetaValidProc() findRepoMetaValidProc}
   *    .localVariableCollector(io.jpm.core.jpm_repository.steps.find_repo_meta_handler.infra.utils.LocalVariableCollector) // required {@link GlobalRegistry#localVariableCollector() localVariableCollector}
   *    .build();
   * </pre>
   * @return A new ImmutableGlobalRegistry builder
   */
  public static ImmutableGlobalRegistry.Builder builder() {
    return new ImmutableGlobalRegistry.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableGlobalRegistry ImmutableGlobalRegistry}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "GlobalRegistry", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TOKEN_EXTRACTOR = 0x1L;
    private static final long INIT_BIT_COMMAND_PROCESSOR = 0x2L;
    private static final long INIT_BIT_MAP_PARAM_REGISTRY = 0x4L;
    private static final long INIT_BIT_SEGMENT_INLINER = 0x8L;
    private static final long INIT_BIT_ERROR_TRACKER = 0x10L;
    private static final long INIT_BIT_FIND_REPO_META_VALID_PROC = 0x20L;
    private static final long INIT_BIT_LOCAL_VARIABLE_COLLECTOR = 0x40L;
    private long initBits = 0x7fL;

    private Map<String, String> options = new LinkedHashMap<String, String>();
    private @Nullable ArgumentTokenExtractor tokenExtractor;
    private @Nullable DslCommandProcStep commandProcessor;
    private @Nullable MapParamRegistryImpl mapParamRegistry;
    private @Nullable SegmentInlinerStep segmentInliner;
    private @Nullable ErrorTracker errorTracker;
    private @Nullable DslCommandProcessorValidStep findRepoMetaValidProc;
    private @Nullable LocalVariableCollector localVariableCollector;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code GlobalRegistry} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(GlobalRegistry instance) {
      Objects.requireNonNull(instance, "instance");
      putAllOptions(instance.options());
      this.tokenExtractor(instance.tokenExtractor());
      this.commandProcessor(instance.commandProcessor());
      this.mapParamRegistry(instance.mapParamRegistry());
      this.segmentInliner(instance.segmentInliner());
      this.errorTracker(instance.errorTracker());
      this.findRepoMetaValidProc(instance.findRepoMetaValidProc());
      this.localVariableCollector(instance.localVariableCollector());
      return this;
    }

    /**
     * Put one entry to the {@link GlobalRegistry#options() options} map.
     * @param key The key in the options map
     * @param value The associated value in the options map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putOptions(String key, String value) {
      this.options.put(
          Objects.requireNonNull(key, "options key"),
          Objects.requireNonNull(value, value == null ? "options value for key: " + key : null));
      return this;
    }

    /**
     * Put one entry to the {@link GlobalRegistry#options() options} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putOptions(Map.Entry<String, ? extends String> entry) {
      String k = entry.getKey();
      String v = entry.getValue();
      this.options.put(
          Objects.requireNonNull(k, "options key"),
          Objects.requireNonNull(v, v == null ? "options value for key: " + k : null));
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link GlobalRegistry#options() options} map. Nulls are not permitted
     * @param entries The entries that will be added to the options map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder options(Map<String, ? extends String> entries) {
      this.options.clear();
      return putAllOptions(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link GlobalRegistry#options() options} map. Nulls are not permitted
     * @param entries The entries that will be added to the options map
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder putAllOptions(Map<String, ? extends String> entries) {
      for (Map.Entry<String, ? extends String> e : entries.entrySet()) {
        String k = e.getKey();
        String v = e.getValue();
        this.options.put(
            Objects.requireNonNull(k, "options key"),
            Objects.requireNonNull(v, v == null ? "options value for key: " + k : null));
      }
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#tokenExtractor() tokenExtractor} attribute.
     * @param tokenExtractor The value for tokenExtractor 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder tokenExtractor(ArgumentTokenExtractor tokenExtractor) {
      this.tokenExtractor = Objects.requireNonNull(tokenExtractor, "tokenExtractor");
      initBits &= ~INIT_BIT_TOKEN_EXTRACTOR;
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#commandProcessor() commandProcessor} attribute.
     * @param commandProcessor The value for commandProcessor 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder commandProcessor(DslCommandProcStep commandProcessor) {
      this.commandProcessor = Objects.requireNonNull(commandProcessor, "commandProcessor");
      initBits &= ~INIT_BIT_COMMAND_PROCESSOR;
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#mapParamRegistry() mapParamRegistry} attribute.
     * @param mapParamRegistry The value for mapParamRegistry 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder mapParamRegistry(MapParamRegistryImpl mapParamRegistry) {
      this.mapParamRegistry = Objects.requireNonNull(mapParamRegistry, "mapParamRegistry");
      initBits &= ~INIT_BIT_MAP_PARAM_REGISTRY;
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#segmentInliner() segmentInliner} attribute.
     * @param segmentInliner The value for segmentInliner 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder segmentInliner(SegmentInlinerStep segmentInliner) {
      this.segmentInliner = Objects.requireNonNull(segmentInliner, "segmentInliner");
      initBits &= ~INIT_BIT_SEGMENT_INLINER;
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#errorTracker() errorTracker} attribute.
     * @param errorTracker The value for errorTracker 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder errorTracker(ErrorTracker errorTracker) {
      this.errorTracker = Objects.requireNonNull(errorTracker, "errorTracker");
      initBits &= ~INIT_BIT_ERROR_TRACKER;
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#findRepoMetaValidProc() findRepoMetaValidProc} attribute.
     * @param findRepoMetaValidProc The value for findRepoMetaValidProc 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder findRepoMetaValidProc(DslCommandProcessorValidStep findRepoMetaValidProc) {
      this.findRepoMetaValidProc = Objects.requireNonNull(findRepoMetaValidProc, "findRepoMetaValidProc");
      initBits &= ~INIT_BIT_FIND_REPO_META_VALID_PROC;
      return this;
    }

    /**
     * Initializes the value for the {@link GlobalRegistry#localVariableCollector() localVariableCollector} attribute.
     * @param localVariableCollector The value for localVariableCollector 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder localVariableCollector(LocalVariableCollector localVariableCollector) {
      this.localVariableCollector = Objects.requireNonNull(localVariableCollector, "localVariableCollector");
      initBits &= ~INIT_BIT_LOCAL_VARIABLE_COLLECTOR;
      return this;
    }

    /**
     * Builds a new {@link ImmutableGlobalRegistry ImmutableGlobalRegistry}.
     * @return An immutable instance of GlobalRegistry
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableGlobalRegistry build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableGlobalRegistry(
          createUnmodifiableMap(false, false, options),
          tokenExtractor,
          commandProcessor,
          mapParamRegistry,
          segmentInliner,
          errorTracker,
          findRepoMetaValidProc,
          localVariableCollector);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TOKEN_EXTRACTOR) != 0) attributes.add("tokenExtractor");
      if ((initBits & INIT_BIT_COMMAND_PROCESSOR) != 0) attributes.add("commandProcessor");
      if ((initBits & INIT_BIT_MAP_PARAM_REGISTRY) != 0) attributes.add("mapParamRegistry");
      if ((initBits & INIT_BIT_SEGMENT_INLINER) != 0) attributes.add("segmentInliner");
      if ((initBits & INIT_BIT_ERROR_TRACKER) != 0) attributes.add("errorTracker");
      if ((initBits & INIT_BIT_FIND_REPO_META_VALID_PROC) != 0) attributes.add("findRepoMetaValidProc");
      if ((initBits & INIT_BIT_LOCAL_VARIABLE_COLLECTOR) != 0) attributes.add("localVariableCollector");
      return "Cannot build GlobalRegistry, some of required attributes are not set " + attributes;
    }
  }

  private static <K, V> Map<K, V> createUnmodifiableMap(boolean checkNulls, boolean skipNulls, Map<? extends K, ? extends V> map) {
    switch (map.size()) {
    case 0: return Collections.emptyMap();
    case 1: {
      Map.Entry<? extends K, ? extends V> e = map.entrySet().iterator().next();
      K k = e.getKey();
      V v = e.getValue();
      if (checkNulls) {
        Objects.requireNonNull(k, "key");
        Objects.requireNonNull(v, v == null ? "value for key: " + k : null);
      }
      if (skipNulls && (k == null || v == null)) {
        return Collections.emptyMap();
      }
      return Collections.singletonMap(k, v);
    }
    default: {
      Map<K, V> linkedMap = new LinkedHashMap<>(map.size() * 4 / 3 + 1);
      if (skipNulls || checkNulls) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
          K k = e.getKey();
          V v = e.getValue();
          if (skipNulls) {
            if (k == null || v == null) continue;
          } else if (checkNulls) {
            Objects.requireNonNull(k, "key");
            Objects.requireNonNull(v, v == null ? "value for key: " + k : null);
          }
          linkedMap.put(k, v);
        }
      } else {
        linkedMap.putAll(map);
      }
      return Collections.unmodifiableMap(linkedMap);
    }
    }
  }
}
