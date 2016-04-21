package cn.com.cig.adsense.vo.fix;

import com.twitter.scrooge.Utilities;
import com.twitter.scrooge.ThriftStruct;
import com.twitter.scrooge.ThriftStructCodec;
import com.twitter.scrooge.ThriftStructCodec3;

import org.apache.thrift.protocol.*;

import java.util.Map;
import java.util.HashMap;


@javax.annotation.Generated(value = "com.twitter.scrooge.Compiler", date = "2014-12-06T02:04:33.859+0800")
public class UserStruct implements ThriftStruct {
  private static final TStruct STRUCT = new TStruct("userStruct");
  private static final TField UserIdField = new TField("user_id", TType.STRING, (short) 1);
  final String userId;
  private static final TField MtagField = new TField("mtag", TType.MAP, (short) 2);
  final Map<Integer, Integer> mtag;
  private static final TField OtagField = new TField("otag", TType.MAP, (short) 3);
  final Map<Integer, Integer> otag;
  private static final TField LastModelField = new TField("last_model", TType.I32, (short) 4);
  final int lastModel;
  private static final TField CityField = new TField("city", TType.I32, (short) 5);
  final int city;
  private static final TField ProvinceField = new TField("province", TType.I32, (short) 6);
  final int province;
  private static final TField CreateTimeField = new TField("createTime", TType.STRING, (short) 7);
  final String createTime;
  private static final TField LastVisitField = new TField("last_visit", TType.STRING, (short) 8);
  final String lastVisit;

  @SuppressWarnings("unchecked")
  public static class Builder {
    private String _userId = null;

    public Builder userId(String value) {
      this._userId = value;
      return this;
    }

    public Builder unsetUserId() {
      this._userId = null;
      return this;
    }
    private Map<Integer, Integer> _mtag = Utilities.makeMap();

    public Builder mtag(Map<Integer, Integer> value) {
      this._mtag = value;
      return this;
    }

   
	public Builder unsetMtag() {
      this._mtag = Utilities.makeMap();
      return this;
    }
    private Map<Integer, Integer> _otag = Utilities.makeMap();

    public Builder otag(Map<Integer, Integer> value) {
      this._otag = value;
      return this;
    }

    public Builder unsetOtag() {
      this._otag = Utilities.makeMap();
      return this;
    }
    private int _lastModel = 0;

    public Builder lastModel(int value) {
      this._lastModel = value;
      return this;
    }

    public Builder unsetLastModel() {
      this._lastModel = 0;
      return this;
    }
    private int _city = 0;

    public Builder city(int value) {
      this._city = value;
      return this;
    }

    public Builder unsetCity() {
      this._city = 0;
      return this;
    }
    private int _province = 0;

    public Builder province(int value) {
      this._province = value;
      return this;
    }

    public Builder unsetProvince() {
      this._province = 0;
      return this;
    }
    private String _createTime = null;

    public Builder createTime(String value) {
      this._createTime = value;
      return this;
    }

    public Builder unsetCreateTime() {
      this._createTime = null;
      return this;
    }
    private String _lastVisit = null;

    public Builder lastVisit(String value) {
      this._lastVisit = value;
      return this;
    }

    public Builder unsetLastVisit() {
      this._lastVisit = null;
      return this;
    }

    public UserStruct build() {
      return new UserStruct(
        this._userId,
        this._mtag,
        this._otag,
        this._lastModel,
        this._city,
        this._province,
        this._createTime,
        this._lastVisit    );
    }
  }

  public Builder copy() {
    Builder builder = new Builder();
    builder.userId(this.userId);
    builder.mtag(this.mtag);
    builder.otag(this.otag);
    builder.lastModel(this.lastModel);
    builder.city(this.city);
    builder.province(this.province);
    builder.createTime(this.createTime);
    builder.lastVisit(this.lastVisit);
    return builder;
  }

  @SuppressWarnings("unchecked")
  public static ThriftStructCodec<UserStruct> CODEC = new ThriftStructCodec3<UserStruct>() {
    @Override
    public UserStruct decode(TProtocol _iprot) throws org.apache.thrift.TException {
      Builder builder = new Builder();
      String userId = null;
      Map<Integer, Integer> mtag = Utilities.makeMap();
      Map<Integer, Integer> otag = Utilities.makeMap();
      int lastModel = 0;
      int city = 0;
      int province = 0;
      String createTime = null;
      String lastVisit = null;
      Boolean _done = false;
      _iprot.readStructBegin();
      while (!_done) {
        TField _field = _iprot.readFieldBegin();
        if (_field.type == TType.STOP) {
          _done = true;
        } else {
          switch (_field.id) {
            case 1: /* userId */
              switch (_field.type) {
                case TType.STRING:
                  String userId_item;
                  userId_item = _iprot.readString();
                  userId = userId_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.userId(userId);
              break;
            case 2: /* mtag */
              switch (_field.type) {
                case TType.MAP:
                  Map<Integer, Integer> mtag_item;
                  TMap _map_mtag_item = _iprot.readMapBegin();
                  mtag_item = new HashMap<Integer, Integer>();
                  int _i_mtag_item = 0;
                  Integer mtag_item_key;
                  Integer mtag_item_value;
                  while (_i_mtag_item < _map_mtag_item.size) {
                    mtag_item_key = _iprot.readI32();
                    mtag_item_value = _iprot.readI32();
                    mtag_item.put(mtag_item_key, mtag_item_value);
                    _i_mtag_item += 1;
                  }
                  _iprot.readMapEnd();
                  mtag = mtag_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.mtag(mtag);
              break;
            case 3: /* otag */
              switch (_field.type) {
                case TType.MAP:
                  Map<Integer, Integer> otag_item;
                  TMap _map_otag_item = _iprot.readMapBegin();
                  otag_item = new HashMap<Integer, Integer>();
                  int _i_otag_item = 0;
                  Integer otag_item_key;
                  Integer otag_item_value;
                  while (_i_otag_item < _map_otag_item.size) {
                    otag_item_key = _iprot.readI32();
                    otag_item_value = _iprot.readI32();
                    otag_item.put(otag_item_key, otag_item_value);
                    _i_otag_item += 1;
                  }
                  _iprot.readMapEnd();
                  otag = otag_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.otag(otag);
              break;
            case 4: /* lastModel */
              switch (_field.type) {
                case TType.I32:
                  Integer lastModel_item;
                  lastModel_item = _iprot.readI32();
                  lastModel = lastModel_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.lastModel(lastModel);
              break;
            case 5: /* city */
              switch (_field.type) {
                case TType.I32:
                  Integer city_item;
                  city_item = _iprot.readI32();
                  city = city_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.city(city);
              break;
            case 6: /* province */
              switch (_field.type) {
                case TType.I32:
                  Integer province_item;
                  province_item = _iprot.readI32();
                  province = province_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.province(province);
              break;
            case 7: /* createTime */
              switch (_field.type) {
                case TType.STRING:
                  String createTime_item;
                  createTime_item = _iprot.readString();
                  createTime = createTime_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.createTime(createTime);
              break;
            case 8: /* lastVisit */
              switch (_field.type) {
                case TType.STRING:
                  String lastVisit_item;
                  lastVisit_item = _iprot.readString();
                  lastVisit = lastVisit_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.lastVisit(lastVisit);
              break;
            default:
              TProtocolUtil.skip(_iprot, _field.type);
          }
          _iprot.readFieldEnd();
        }
      }
      _iprot.readStructEnd();
      try {
        return builder.build();
      } catch (IllegalStateException stateEx) {
        throw new TProtocolException(stateEx.getMessage());
      }
    }

    @Override
    public void encode(UserStruct struct, TProtocol oprot) throws org.apache.thrift.TException {
      struct.write(oprot);
    }
  };

  public static UserStruct decode(TProtocol _iprot) throws org.apache.thrift.TException {
    return CODEC.decode(_iprot);
  }

  public static void encode(UserStruct struct, TProtocol oprot) throws org.apache.thrift.TException {
    CODEC.encode(struct, oprot);
  }

  public UserStruct(
    String userId, 
    Map<Integer, Integer> mtag, 
    Map<Integer, Integer> otag, 
    int lastModel, 
    int city, 
    int province, 
    String createTime, 
    String lastVisit
  ) {
    this.userId = userId;
    this.mtag = mtag;
    this.otag = otag;
    this.lastModel = lastModel;
    this.city = city;
    this.province = province;
    this.createTime = createTime;
    this.lastVisit = lastVisit;
  }


  public String getUserId() {
    return this.userId;
  }
  public boolean isSetUserId() {
    return this.userId != null;
  }
  public Map<Integer, Integer> getMtag() {
    return this.mtag;
  }
  public boolean isSetMtag() {
    return this.mtag != null;
  }
  public Map<Integer, Integer> getOtag() {
    return this.otag;
  }
  public boolean isSetOtag() {
    return this.otag != null;
  }
  public int getLastModel() {
    return this.lastModel;
  }
  public int getCity() {
    return this.city;
  }
  public int getProvince() {
    return this.province;
  }
  public String getCreateTime() {
    return this.createTime;
  }
  public boolean isSetCreateTime() {
    return this.createTime != null;
  }
  public String getLastVisit() {
    return this.lastVisit;
  }
  public boolean isSetLastVisit() {
    return this.lastVisit != null;
  }

  public void write(TProtocol _oprot) throws org.apache.thrift.TException {
    validate();
    _oprot.writeStructBegin(STRUCT);
      _oprot.writeFieldBegin(UserIdField);
      String userId_item = userId;
      _oprot.writeString(userId_item);
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(MtagField);
      Map<Integer, Integer> mtag_item = mtag;
      _oprot.writeMapBegin(new TMap(TType.I32, TType.I32, mtag_item.size()));
      for (Map.Entry<Integer, Integer> _mtag_item_entry : mtag_item.entrySet()) {
        Integer mtag_item_key = _mtag_item_entry.getKey();
        _oprot.writeI32(mtag_item_key);
        Integer mtag_item_value = _mtag_item_entry.getValue();
        _oprot.writeI32(mtag_item_value);
      }
      _oprot.writeMapEnd();
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(OtagField);
      Map<Integer, Integer> otag_item = otag;
      _oprot.writeMapBegin(new TMap(TType.I32, TType.I32, otag_item.size()));
      for (Map.Entry<Integer, Integer> _otag_item_entry : otag_item.entrySet()) {
        Integer otag_item_key = _otag_item_entry.getKey();
        _oprot.writeI32(otag_item_key);
        Integer otag_item_value = _otag_item_entry.getValue();
        _oprot.writeI32(otag_item_value);
      }
      _oprot.writeMapEnd();
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(LastModelField);
      Integer lastModel_item = lastModel;
      _oprot.writeI32(lastModel_item);
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(CityField);
      Integer city_item = city;
      _oprot.writeI32(city_item);
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(ProvinceField);
      Integer province_item = province;
      _oprot.writeI32(province_item);
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(CreateTimeField);
      String createTime_item = createTime;
      _oprot.writeString(createTime_item);
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(LastVisitField);
      String lastVisit_item = lastVisit;
      _oprot.writeString(lastVisit_item);
      _oprot.writeFieldEnd();
    _oprot.writeFieldStop();
    _oprot.writeStructEnd();
  }

  private void validate() throws org.apache.thrift.protocol.TProtocolException {
  }


  @Override
  public boolean equals(Object other) {
    if (!(other instanceof UserStruct)) return false;
    UserStruct that = (UserStruct) other;
    return
this.userId.equals(that.userId) &&
this.mtag.equals(that.mtag) &&
this.otag.equals(that.otag) &&
      this.lastModel == that.lastModel
 &&
      this.city == that.city
 &&
      this.province == that.province
 &&
this.createTime.equals(that.createTime) &&
this.lastVisit.equals(that.lastVisit);
  }

  @Override
  public String toString() {
    return "UserStruct(" + this.userId + "," + this.mtag + "," + this.otag + "," + this.lastModel + "," + this.city + "," + this.province + "," + this.createTime + "," + this.lastVisit + ")";
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * (this.userId == null ? 0 : this.userId.hashCode());
    hash = hash * (this.mtag == null ? 0 : this.mtag.hashCode());
    hash = hash * (this.otag == null ? 0 : this.otag.hashCode());
    hash = hash * new Integer(this.lastModel).hashCode();
    hash = hash * new Integer(this.city).hashCode();
    hash = hash * new Integer(this.province).hashCode();
    hash = hash * (this.createTime == null ? 0 : this.createTime.hashCode());
    hash = hash * (this.lastVisit == null ? 0 : this.lastVisit.hashCode());
    return hash;
  }
}