package cn.com.cig.adsense.vo.fix;

import com.twitter.scrooge.Option;
import com.twitter.scrooge.ThriftStruct;
import com.twitter.scrooge.ThriftStructCodec;
import com.twitter.scrooge.ThriftStructCodec3;
import org.apache.thrift.protocol.*;

@javax.annotation.Generated(value = "com.twitter.scrooge.Compiler", date = "2014-12-06T02:04:33.859+0800")
public class UserResult implements ThriftStruct {
  private static final TStruct STRUCT = new TStruct("UserResult");
  private static final TField UserField = new TField("user", TType.STRUCT, (short) 1);
  final Option<UserStruct> user;
  private static final TField ResultField = new TField("result", TType.I32, (short) 2);
  final Result result;
  private static final TField ErrorMsgField = new TField("errorMsg", TType.STRING, (short) 3);
  final Option<String> errorMsg;

  public static class Builder {
    private UserStruct _user = null;
    private Boolean _got_user = false;

    public Builder user(UserStruct value) {
      this._user = value;
      this._got_user = true;
      return this;
    }

    public Builder unsetUser() {
      this._user = null;
      this._got_user = false;
      return this;
    }
    private Result _result = null;

    public Builder result(Result value) {
      this._result = value;
      return this;
    }

    public Builder unsetResult() {
      this._result = null;
      return this;
    }
    private String _errorMsg = null;
    private Boolean _got_errorMsg = false;

    public Builder errorMsg(String value) {
      this._errorMsg = value;
      this._got_errorMsg = true;
      return this;
    }

    public Builder unsetErrorMsg() {
      this._errorMsg = null;
      this._got_errorMsg = false;
      return this;
    }

    public UserResult build() {
      return new UserResult(
      Option.make(this._got_user, this._user),
        this._result,
      Option.make(this._got_errorMsg, this._errorMsg)    );
    }
  }

  public Builder copy() {
    Builder builder = new Builder();
    if (this.user.isDefined()) builder.user(this.user.get());
    builder.result(this.result);
    if (this.errorMsg.isDefined()) builder.errorMsg(this.errorMsg.get());
    return builder;
  }

  public static ThriftStructCodec<UserResult> CODEC = new ThriftStructCodec3<UserResult>() {
    @Override
    public UserResult decode(TProtocol _iprot) throws org.apache.thrift.TException {
      Builder builder = new Builder();
      UserStruct user = null;
      Result result = null;
      String errorMsg = null;
      Boolean _done = false;
      _iprot.readStructBegin();
      while (!_done) {
        TField _field = _iprot.readFieldBegin();
        if (_field.type == TType.STOP) {
          _done = true;
        } else {
          switch (_field.id) {
            case 1: /* user */
              switch (_field.type) {
                case TType.STRUCT:
                  UserStruct user_item;
                  user_item = UserStruct.decode(_iprot);
                  user = user_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.user(user);
              break;
            case 2: /* result */
              switch (_field.type) {
                case TType.I32:
                  Result result_item;
                  result_item = Result.findByValue(_iprot.readI32());
                  result = result_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.result(result);
              break;
            case 3: /* errorMsg */
              switch (_field.type) {
                case TType.STRING:
                  String errorMsg_item;
                  errorMsg_item = _iprot.readString();
                  errorMsg = errorMsg_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.errorMsg(errorMsg);
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
    public void encode(UserResult struct, TProtocol oprot) throws org.apache.thrift.TException {
      struct.write(oprot);
    }
  };

  public static UserResult decode(TProtocol _iprot) throws org.apache.thrift.TException {
    return CODEC.decode(_iprot);
  }

  public static void encode(UserResult struct, TProtocol oprot) throws org.apache.thrift.TException {
    CODEC.encode(struct, oprot);
  }

  public UserResult(
    Option<UserStruct> user, 
    Result result, 
    Option<String> errorMsg
  ) {
    this.user = user;
    this.result = result;
    this.errorMsg = errorMsg;
  }

  public UserResult(
    Result result
  ) {
    this.user = Option.none();
    this.result = result;
    this.errorMsg = Option.none();
  }

  public UserStruct getUser() {
    return this.user.get();
  }
  public boolean isSetUser() {
    return this.user.isDefined();
  }
  public Result getResult() {
    return this.result;
  }
  public boolean isSetResult() {
    return this.result != null;
  }
  public String getErrorMsg() {
    return this.errorMsg.get();
  }
  public boolean isSetErrorMsg() {
    return this.errorMsg.isDefined();
  }

  public void write(TProtocol _oprot) throws org.apache.thrift.TException {
    validate();
    _oprot.writeStructBegin(STRUCT);
    if (user.isDefined()) {  _oprot.writeFieldBegin(UserField);
      UserStruct user_item = user.get();
      user_item.write(_oprot);
      _oprot.writeFieldEnd();
    }
      _oprot.writeFieldBegin(ResultField);
      Result result_item = result;
      _oprot.writeI32(result_item.getValue());
      _oprot.writeFieldEnd();
    if (errorMsg.isDefined()) {  _oprot.writeFieldBegin(ErrorMsgField);
      String errorMsg_item = errorMsg.get();
      _oprot.writeString(errorMsg_item);
      _oprot.writeFieldEnd();
    }
    _oprot.writeFieldStop();
    _oprot.writeStructEnd();
  }

  private void validate() throws org.apache.thrift.protocol.TProtocolException {
  }


  @Override
  public boolean equals(Object other) {
    if (!(other instanceof UserResult)) return false;
    UserResult that = (UserResult) other;
    return
this.user.equals(that.user) &&
this.result.equals(that.result) &&
this.errorMsg.equals(that.errorMsg);
  }

  @Override
  public String toString() {
    return "UserResult(" + this.user + "," + this.result + "," + this.errorMsg + ")";
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * (this.user.isDefined() ? 0 : this.user.get().hashCode());
    hash = hash * (this.result == null ? 0 : this.result.hashCode());
    hash = hash * (this.errorMsg.isDefined() ? 0 : this.errorMsg.get().hashCode());
    return hash;
  }
}