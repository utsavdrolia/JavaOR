package org.crowdcache.objrec.rpc;// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: objrec.proto

public final class ObjRecServiceProto {
  private ObjRecServiceProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ImageOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Image)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required bytes image = 1;</code>
     */
    boolean hasImage();
    /**
     * <code>required bytes image = 1;</code>
     */
    com.google.protobuf.ByteString getImage();
  }
  /**
   * Protobuf type {@code Image}
   */
  public  static final class Image extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:Image)
      ImageOrBuilder {
    // Use Image.newBuilder() to construct.
    private Image(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private Image() {
      image_ = com.google.protobuf.ByteString.EMPTY;
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Image(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              image_ = input.readBytes();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ObjRecServiceProto.internal_static_Image_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ObjRecServiceProto.internal_static_Image_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              Image.class, Builder.class);
    }

    private int bitField0_;
    public static final int IMAGE_FIELD_NUMBER = 1;
    private com.google.protobuf.ByteString image_;
    /**
     * <code>required bytes image = 1;</code>
     */
    public boolean hasImage() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required bytes image = 1;</code>
     */
    public com.google.protobuf.ByteString getImage() {
      return image_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasImage()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, image_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, image_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    public static Image parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Image parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Image parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Image parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Image parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static Image parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static Image parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static Image parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static Image parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static Image parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(Image prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Image}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Image)
        ImageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ObjRecServiceProto.internal_static_Image_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ObjRecServiceProto.internal_static_Image_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                Image.class, Builder.class);
      }

      // Construct using ObjRecServiceProto.Image.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        image_ = com.google.protobuf.ByteString.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ObjRecServiceProto.internal_static_Image_descriptor;
      }

      public Image getDefaultInstanceForType() {
        return Image.getDefaultInstance();
      }

      public Image build() {
        Image result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public Image buildPartial() {
        Image result = new Image(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.image_ = image_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof Image) {
          return mergeFrom((Image)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(Image other) {
        if (other == Image.getDefaultInstance()) return this;
        if (other.hasImage()) {
          setImage(other.getImage());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        if (!hasImage()) {
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        Image parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (Image) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private com.google.protobuf.ByteString image_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>required bytes image = 1;</code>
       */
      public boolean hasImage() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required bytes image = 1;</code>
       */
      public com.google.protobuf.ByteString getImage() {
        return image_;
      }
      /**
       * <code>required bytes image = 1;</code>
       */
      public Builder setImage(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        image_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required bytes image = 1;</code>
       */
      public Builder clearImage() {
        bitField0_ = (bitField0_ & ~0x00000001);
        image_ = getDefaultInstance().getImage();
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:Image)
    }

    // @@protoc_insertion_point(class_scope:Image)
    private static final Image DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new Image();
    }

    public static Image getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @Deprecated public static final com.google.protobuf.Parser<Image>
        PARSER = new com.google.protobuf.AbstractParser<Image>() {
      public Image parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new Image(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Image> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<Image> getParserForType() {
      return PARSER;
    }

    public Image getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface AnnotationOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Annotation)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required string annotation = 1;</code>
     */
    boolean hasAnnotation();
    /**
     * <code>required string annotation = 1;</code>
     */
    String getAnnotation();
    /**
     * <code>required string annotation = 1;</code>
     */
    com.google.protobuf.ByteString
        getAnnotationBytes();
  }
  /**
   * Protobuf type {@code Annotation}
   */
  public  static final class Annotation extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:Annotation)
      AnnotationOrBuilder {
    // Use Annotation.newBuilder() to construct.
    private Annotation(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private Annotation() {
      annotation_ = "";
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private Annotation(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000001;
              annotation_ = bs;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return ObjRecServiceProto.internal_static_Annotation_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return ObjRecServiceProto.internal_static_Annotation_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              Annotation.class, Builder.class);
    }

    private int bitField0_;
    public static final int ANNOTATION_FIELD_NUMBER = 1;
    private volatile Object annotation_;
    /**
     * <code>required string annotation = 1;</code>
     */
    public boolean hasAnnotation() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string annotation = 1;</code>
     */
    public String getAnnotation() {
      Object ref = annotation_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          annotation_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string annotation = 1;</code>
     */
    public com.google.protobuf.ByteString
        getAnnotationBytes() {
      Object ref = annotation_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        annotation_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasAnnotation()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 1, annotation_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(1, annotation_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    public static Annotation parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Annotation parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Annotation parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static Annotation parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static Annotation parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static Annotation parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static Annotation parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static Annotation parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static Annotation parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static Annotation parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(Annotation prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Annotation}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Annotation)
        AnnotationOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return ObjRecServiceProto.internal_static_Annotation_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return ObjRecServiceProto.internal_static_Annotation_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                Annotation.class, Builder.class);
      }

      // Construct using ObjRecServiceProto.Annotation.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        annotation_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return ObjRecServiceProto.internal_static_Annotation_descriptor;
      }

      public Annotation getDefaultInstanceForType() {
        return Annotation.getDefaultInstance();
      }

      public Annotation build() {
        Annotation result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public Annotation buildPartial() {
        Annotation result = new Annotation(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.annotation_ = annotation_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof Annotation) {
          return mergeFrom((Annotation)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(Annotation other) {
        if (other == Annotation.getDefaultInstance()) return this;
        if (other.hasAnnotation()) {
          bitField0_ |= 0x00000001;
          annotation_ = other.annotation_;
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        if (!hasAnnotation()) {
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        Annotation parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (Annotation) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private Object annotation_ = "";
      /**
       * <code>required string annotation = 1;</code>
       */
      public boolean hasAnnotation() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string annotation = 1;</code>
       */
      public String getAnnotation() {
        Object ref = annotation_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            annotation_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>required string annotation = 1;</code>
       */
      public com.google.protobuf.ByteString
          getAnnotationBytes() {
        Object ref = annotation_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          annotation_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string annotation = 1;</code>
       */
      public Builder setAnnotation(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        annotation_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string annotation = 1;</code>
       */
      public Builder clearAnnotation() {
        bitField0_ = (bitField0_ & ~0x00000001);
        annotation_ = getDefaultInstance().getAnnotation();
        onChanged();
        return this;
      }
      /**
       * <code>required string annotation = 1;</code>
       */
      public Builder setAnnotationBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        annotation_ = value;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:Annotation)
    }

    // @@protoc_insertion_point(class_scope:Annotation)
    private static final Annotation DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new Annotation();
    }

    public static Annotation getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @Deprecated public static final com.google.protobuf.Parser<Annotation>
        PARSER = new com.google.protobuf.AbstractParser<Annotation>() {
      public Annotation parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new Annotation(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<Annotation> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<Annotation> getParserForType() {
      return PARSER;
    }

    public Annotation getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  /**
   * Protobuf service {@code ObjRecService}
   */
  public static abstract class ObjRecService
      implements com.google.protobuf.Service {
    protected ObjRecService() {}

    public interface Interface {
      /**
       * <code>rpc Recognize(.Image) returns (.Annotation);</code>
       */
      public abstract void recognize(
              com.google.protobuf.RpcController controller,
              Image request,
              com.google.protobuf.RpcCallback<Annotation> done);

    }

    public static com.google.protobuf.Service newReflectiveService(
        final Interface impl) {
      return new ObjRecService() {
        @Override
        public  void recognize(
            com.google.protobuf.RpcController controller,
            Image request,
            com.google.protobuf.RpcCallback<Annotation> done) {
          impl.recognize(controller, request, done);
        }

      };
    }

    public static com.google.protobuf.BlockingService
        newReflectiveBlockingService(final BlockingInterface impl) {
      return new com.google.protobuf.BlockingService() {
        public final com.google.protobuf.Descriptors.ServiceDescriptor
            getDescriptorForType() {
          return getDescriptor();
        }

        public final com.google.protobuf.Message callBlockingMethod(
            com.google.protobuf.Descriptors.MethodDescriptor method,
            com.google.protobuf.RpcController controller,
            com.google.protobuf.Message request)
            throws com.google.protobuf.ServiceException {
          if (method.getService() != getDescriptor()) {
            throw new IllegalArgumentException(
              "Service.callBlockingMethod() given method descriptor for " +
              "wrong service type.");
          }
          switch(method.getIndex()) {
            case 0:
              return impl.recognize(controller, (Image)request);
            default:
              throw new AssertionError("Can't get here.");
          }
        }

        public final com.google.protobuf.Message
            getRequestPrototype(
            com.google.protobuf.Descriptors.MethodDescriptor method) {
          if (method.getService() != getDescriptor()) {
            throw new IllegalArgumentException(
              "Service.getRequestPrototype() given method " +
              "descriptor for wrong service type.");
          }
          switch(method.getIndex()) {
            case 0:
              return Image.getDefaultInstance();
            default:
              throw new AssertionError("Can't get here.");
          }
        }

        public final com.google.protobuf.Message
            getResponsePrototype(
            com.google.protobuf.Descriptors.MethodDescriptor method) {
          if (method.getService() != getDescriptor()) {
            throw new IllegalArgumentException(
              "Service.getResponsePrototype() given method " +
              "descriptor for wrong service type.");
          }
          switch(method.getIndex()) {
            case 0:
              return Annotation.getDefaultInstance();
            default:
              throw new AssertionError("Can't get here.");
          }
        }

      };
    }

    /**
     * <code>rpc Recognize(.Image) returns (.Annotation);</code>
     */
    public abstract void recognize(
        com.google.protobuf.RpcController controller,
        Image request,
        com.google.protobuf.RpcCallback<Annotation> done);

    public static final
        com.google.protobuf.Descriptors.ServiceDescriptor
        getDescriptor() {
      return ObjRecServiceProto.getDescriptor().getServices().get(0);
    }
    public final com.google.protobuf.Descriptors.ServiceDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }

    public final void callMethod(
        com.google.protobuf.Descriptors.MethodDescriptor method,
        com.google.protobuf.RpcController controller,
        com.google.protobuf.Message request,
        com.google.protobuf.RpcCallback<
          com.google.protobuf.Message> done) {
      if (method.getService() != getDescriptor()) {
        throw new IllegalArgumentException(
          "Service.callMethod() given method descriptor for wrong " +
          "service type.");
      }
      switch(method.getIndex()) {
        case 0:
          this.recognize(controller, (Image)request,
            com.google.protobuf.RpcUtil.<Annotation>specializeCallback(
              done));
          return;
        default:
          throw new AssertionError("Can't get here.");
      }
    }

    public final com.google.protobuf.Message
        getRequestPrototype(
        com.google.protobuf.Descriptors.MethodDescriptor method) {
      if (method.getService() != getDescriptor()) {
        throw new IllegalArgumentException(
          "Service.getRequestPrototype() given method " +
          "descriptor for wrong service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return Image.getDefaultInstance();
        default:
          throw new AssertionError("Can't get here.");
      }
    }

    public final com.google.protobuf.Message
        getResponsePrototype(
        com.google.protobuf.Descriptors.MethodDescriptor method) {
      if (method.getService() != getDescriptor()) {
        throw new IllegalArgumentException(
          "Service.getResponsePrototype() given method " +
          "descriptor for wrong service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return Annotation.getDefaultInstance();
        default:
          throw new AssertionError("Can't get here.");
      }
    }

    public static Stub newStub(
        com.google.protobuf.RpcChannel channel) {
      return new Stub(channel);
    }

    public static final class Stub extends ObjRecService implements Interface {
      private Stub(com.google.protobuf.RpcChannel channel) {
        this.channel = channel;
      }

      private final com.google.protobuf.RpcChannel channel;

      public com.google.protobuf.RpcChannel getChannel() {
        return channel;
      }

      public  void recognize(
          com.google.protobuf.RpcController controller,
          Image request,
          com.google.protobuf.RpcCallback<Annotation> done) {
        channel.callMethod(
          getDescriptor().getMethods().get(0),
          controller,
          request,
          Annotation.getDefaultInstance(),
          com.google.protobuf.RpcUtil.generalizeCallback(
            done,
            Annotation.class,
            Annotation.getDefaultInstance()));
      }
    }

    public static BlockingInterface newBlockingStub(
        com.google.protobuf.BlockingRpcChannel channel) {
      return new BlockingStub(channel);
    }

    public interface BlockingInterface {
      public Annotation recognize(
              com.google.protobuf.RpcController controller,
              Image request)
          throws com.google.protobuf.ServiceException;
    }

    private static final class BlockingStub implements BlockingInterface {
      private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
        this.channel = channel;
      }

      private final com.google.protobuf.BlockingRpcChannel channel;

      public Annotation recognize(
          com.google.protobuf.RpcController controller,
          Image request)
          throws com.google.protobuf.ServiceException {
        return (Annotation) channel.callBlockingMethod(
          getDescriptor().getMethods().get(0),
          controller,
          request,
          Annotation.getDefaultInstance());
      }

    }

    // @@protoc_insertion_point(class_scope:ObjRecService)
  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Image_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_Image_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Annotation_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_Annotation_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\014objrec.proto\"\026\n\005Image\022\r\n\005image\030\001 \002(\014\" " +
      "\n\nAnnotation\022\022\n\nannotation\030\001 \002(\t21\n\rObjR" +
      "ecService\022 \n\tRecognize\022\006.Image\032\013.Annotat" +
      "ionB\027B\022ObjRecServiceProto\210\001\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_Image_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Image_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_Image_descriptor,
        new String[] { "Image", });
    internal_static_Annotation_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_Annotation_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_Annotation_descriptor,
        new String[] { "Annotation", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}