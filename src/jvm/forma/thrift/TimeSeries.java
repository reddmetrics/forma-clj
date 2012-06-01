/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package forma.thrift;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeries implements org.apache.thrift.TBase<TimeSeries, TimeSeries._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TimeSeries");

  private static final org.apache.thrift.protocol.TField START_IDX_FIELD_DESC = new org.apache.thrift.protocol.TField("startIdx", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField END_IDX_FIELD_DESC = new org.apache.thrift.protocol.TField("endIdx", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField SERIES_FIELD_DESC = new org.apache.thrift.protocol.TField("series", org.apache.thrift.protocol.TType.STRUCT, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TimeSeriesStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TimeSeriesTupleSchemeFactory());
  }

  public int startIdx; // required
  public int endIdx; // required
  public ArrayValue series; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    START_IDX((short)1, "startIdx"),
    END_IDX((short)2, "endIdx"),
    SERIES((short)3, "series");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // START_IDX
          return START_IDX;
        case 2: // END_IDX
          return END_IDX;
        case 3: // SERIES
          return SERIES;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __STARTIDX_ISSET_ID = 0;
  private static final int __ENDIDX_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.START_IDX, new org.apache.thrift.meta_data.FieldMetaData("startIdx", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.END_IDX, new org.apache.thrift.meta_data.FieldMetaData("endIdx", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SERIES, new org.apache.thrift.meta_data.FieldMetaData("series", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ArrayValue.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TimeSeries.class, metaDataMap);
  }

  public TimeSeries() {
  }

  public TimeSeries(
    int startIdx,
    int endIdx,
    ArrayValue series)
  {
    this();
    this.startIdx = startIdx;
    setStartIdxIsSet(true);
    this.endIdx = endIdx;
    setEndIdxIsSet(true);
    this.series = series;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TimeSeries(TimeSeries other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.startIdx = other.startIdx;
    this.endIdx = other.endIdx;
    if (other.isSetSeries()) {
      this.series = new ArrayValue(other.series);
    }
  }

  public TimeSeries deepCopy() {
    return new TimeSeries(this);
  }

  @Override
  public void clear() {
    setStartIdxIsSet(false);
    this.startIdx = 0;
    setEndIdxIsSet(false);
    this.endIdx = 0;
    this.series = null;
  }

  public int getStartIdx() {
    return this.startIdx;
  }

  public TimeSeries setStartIdx(int startIdx) {
    this.startIdx = startIdx;
    setStartIdxIsSet(true);
    return this;
  }

  public void unsetStartIdx() {
    __isset_bit_vector.clear(__STARTIDX_ISSET_ID);
  }

  /** Returns true if field startIdx is set (has been assigned a value) and false otherwise */
  public boolean isSetStartIdx() {
    return __isset_bit_vector.get(__STARTIDX_ISSET_ID);
  }

  public void setStartIdxIsSet(boolean value) {
    __isset_bit_vector.set(__STARTIDX_ISSET_ID, value);
  }

  public int getEndIdx() {
    return this.endIdx;
  }

  public TimeSeries setEndIdx(int endIdx) {
    this.endIdx = endIdx;
    setEndIdxIsSet(true);
    return this;
  }

  public void unsetEndIdx() {
    __isset_bit_vector.clear(__ENDIDX_ISSET_ID);
  }

  /** Returns true if field endIdx is set (has been assigned a value) and false otherwise */
  public boolean isSetEndIdx() {
    return __isset_bit_vector.get(__ENDIDX_ISSET_ID);
  }

  public void setEndIdxIsSet(boolean value) {
    __isset_bit_vector.set(__ENDIDX_ISSET_ID, value);
  }

  public ArrayValue getSeries() {
    return this.series;
  }

  public TimeSeries setSeries(ArrayValue series) {
    this.series = series;
    return this;
  }

  public void unsetSeries() {
    this.series = null;
  }

  /** Returns true if field series is set (has been assigned a value) and false otherwise */
  public boolean isSetSeries() {
    return this.series != null;
  }

  public void setSeriesIsSet(boolean value) {
    if (!value) {
      this.series = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case START_IDX:
      if (value == null) {
        unsetStartIdx();
      } else {
        setStartIdx((Integer)value);
      }
      break;

    case END_IDX:
      if (value == null) {
        unsetEndIdx();
      } else {
        setEndIdx((Integer)value);
      }
      break;

    case SERIES:
      if (value == null) {
        unsetSeries();
      } else {
        setSeries((ArrayValue)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case START_IDX:
      return Integer.valueOf(getStartIdx());

    case END_IDX:
      return Integer.valueOf(getEndIdx());

    case SERIES:
      return getSeries();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case START_IDX:
      return isSetStartIdx();
    case END_IDX:
      return isSetEndIdx();
    case SERIES:
      return isSetSeries();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TimeSeries)
      return this.equals((TimeSeries)that);
    return false;
  }

  public boolean equals(TimeSeries that) {
    if (that == null)
      return false;

    boolean this_present_startIdx = true;
    boolean that_present_startIdx = true;
    if (this_present_startIdx || that_present_startIdx) {
      if (!(this_present_startIdx && that_present_startIdx))
        return false;
      if (this.startIdx != that.startIdx)
        return false;
    }

    boolean this_present_endIdx = true;
    boolean that_present_endIdx = true;
    if (this_present_endIdx || that_present_endIdx) {
      if (!(this_present_endIdx && that_present_endIdx))
        return false;
      if (this.endIdx != that.endIdx)
        return false;
    }

    boolean this_present_series = true && this.isSetSeries();
    boolean that_present_series = true && that.isSetSeries();
    if (this_present_series || that_present_series) {
      if (!(this_present_series && that_present_series))
        return false;
      if (!this.series.equals(that.series))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();

    boolean present_startIdx = true;
    builder.append(present_startIdx);
    if (present_startIdx)
      builder.append(startIdx);

    boolean present_endIdx = true;
    builder.append(present_endIdx);
    if (present_endIdx)
      builder.append(endIdx);

    boolean present_series = true && (isSetSeries());
    builder.append(present_series);
    if (present_series)
      builder.append(series);

    return builder.toHashCode();
  }

  public int compareTo(TimeSeries other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    TimeSeries typedOther = (TimeSeries)other;

    lastComparison = Boolean.valueOf(isSetStartIdx()).compareTo(typedOther.isSetStartIdx());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartIdx()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startIdx, typedOther.startIdx);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetEndIdx()).compareTo(typedOther.isSetEndIdx());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetEndIdx()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.endIdx, typedOther.endIdx);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSeries()).compareTo(typedOther.isSetSeries());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSeries()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.series, typedOther.series);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TimeSeries(");
    boolean first = true;

    sb.append("startIdx:");
    sb.append(this.startIdx);
    first = false;
    if (!first) sb.append(", ");
    sb.append("endIdx:");
    sb.append(this.endIdx);
    first = false;
    if (!first) sb.append(", ");
    sb.append("series:");
    if (this.series == null) {
      sb.append("null");
    } else {
      sb.append(this.series);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TimeSeriesStandardSchemeFactory implements SchemeFactory {
    public TimeSeriesStandardScheme getScheme() {
      return new TimeSeriesStandardScheme();
    }
  }

  private static class TimeSeriesStandardScheme extends StandardScheme<TimeSeries> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TimeSeries struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // START_IDX
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.startIdx = iprot.readI32();
              struct.setStartIdxIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // END_IDX
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.endIdx = iprot.readI32();
              struct.setEndIdxIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SERIES
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.series = new ArrayValue();
              struct.series.read(iprot);
              struct.setSeriesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TimeSeries struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(START_IDX_FIELD_DESC);
      oprot.writeI32(struct.startIdx);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(END_IDX_FIELD_DESC);
      oprot.writeI32(struct.endIdx);
      oprot.writeFieldEnd();
      if (struct.series != null) {
        oprot.writeFieldBegin(SERIES_FIELD_DESC);
        struct.series.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TimeSeriesTupleSchemeFactory implements SchemeFactory {
    public TimeSeriesTupleScheme getScheme() {
      return new TimeSeriesTupleScheme();
    }
  }

  private static class TimeSeriesTupleScheme extends TupleScheme<TimeSeries> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TimeSeries struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetStartIdx()) {
        optionals.set(0);
      }
      if (struct.isSetEndIdx()) {
        optionals.set(1);
      }
      if (struct.isSetSeries()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetStartIdx()) {
        oprot.writeI32(struct.startIdx);
      }
      if (struct.isSetEndIdx()) {
        oprot.writeI32(struct.endIdx);
      }
      if (struct.isSetSeries()) {
        struct.series.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TimeSeries struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.startIdx = iprot.readI32();
        struct.setStartIdxIsSet(true);
      }
      if (incoming.get(1)) {
        struct.endIdx = iprot.readI32();
        struct.setEndIdxIsSet(true);
      }
      if (incoming.get(2)) {
        struct.series = new ArrayValue();
        struct.series.read(iprot);
        struct.setSeriesIsSet(true);
      }
    }
  }

}
