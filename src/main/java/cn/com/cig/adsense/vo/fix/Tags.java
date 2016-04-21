package cn.com.cig.adsense.vo.fix;

import com.twitter.scrooge.Option;
import com.twitter.scrooge.Utilities;
import com.twitter.scrooge.ThriftStruct;
import com.twitter.scrooge.ThriftStructCodec;
import com.twitter.scrooge.ThriftStructCodec3;
import org.apache.thrift.protocol.*;
import java.util.ArrayList;
import java.util.List;

@javax.annotation.Generated(value = "com.twitter.scrooge.Compiler", date = "2014-12-06T02:04:33.859+0800")
public class Tags implements ThriftStruct {
  private static final TStruct STRUCT = new TStruct("Tags");
  private static final TField ModelField = new TField("model", TType.STRUCT, (short) 1);
  final Option<TagParse> model;
  private static final TField ResultField = new TField("result", TType.I32, (short) 2);
  final Result result;
  private static final TField OtherResultField = new TField("otherResult", TType.LIST, (short) 3);
  final Option<List<TagParse>> otherResult;
  private static final TField ErrorMsgField = new TField("errorMsg", TType.STRING, (short) 4);
  final Option<String> errorMsg;

  public static class Builder {
    private TagParse _model = null;
    private Boolean _got_model = false;

    public Builder model(TagParse value) {
      this._model = value;
      this._got_model = true;
      return this;
    }

    public Builder unsetModel() {
      this._model = null;
      this._got_model = false;
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
    private List<TagParse> _otherResult = Utilities.makeList();
    private Boolean _got_otherResult = false;

    public Builder otherResult(List<TagParse> value) {
      this._otherResult = value;
      this._got_otherResult = true;
      return this;
    }

    public Builder unsetOtherResult() {
      this._otherResult = Utilities.makeList();
      this._got_otherResult = false;
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

    public Tags build() {
      return new Tags(
      Option.make(this._got_model, this._model),
        this._result,
      Option.make(this._got_otherResult, this._otherResult),
      Option.make(this._got_errorMsg, this._errorMsg)    );
    }
  }

  public Builder copy() {
    Builder builder = new Builder();
    if (this.model.isDefined()) builder.model(this.model.get());
    builder.result(this.result);
    if (this.otherResult.isDefined()) builder.otherResult(this.otherResult.get());
    if (this.errorMsg.isDefined()) builder.errorMsg(this.errorMsg.get());
    return builder;
  }

  public static ThriftStructCodec<Tags> CODEC = new ThriftStructCodec3<Tags>() {
    @Override
    public Tags decode(TProtocol _iprot) throws org.apache.thrift.TException {
      Builder builder = new Builder();
      TagParse model = null;
      Result result = null;
      List<TagParse> otherResult = Utilities.makeList();
      String errorMsg = null;
      Boolean _done = false;
      _iprot.readStructBegin();
      while (!_done) {
        TField _field = _iprot.readFieldBegin();
        if (_field.type == TType.STOP) {
          _done = true;
        } else {
          switch (_field.id) {
            case 1: /* model */
              switch (_field.type) {
                case TType.STRUCT:
                  TagParse model_item;
                  model_item = TagParse.decode(_iprot);
                  model = model_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.model(model);
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
            case 3: /* otherResult */
              switch (_field.type) {
                case TType.LIST:
                  List<TagParse> otherResult_item;
                  TList _list_otherResult_item = _iprot.readListBegin();
                  otherResult_item = new ArrayList<TagParse>();
                  int _i_otherResult_item = 0;
                  TagParse otherResult_item_element;
                  while (_i_otherResult_item < _list_otherResult_item.size) {
                    otherResult_item_element = TagParse.decode(_iprot);
                    otherResult_item.add(otherResult_item_element);
                    _i_otherResult_item += 1;
                  }
                  _iprot.readListEnd();
                  otherResult = otherResult_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.otherResult(otherResult);
              break;
            case 4: /* errorMsg */
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
    public void encode(Tags struct, TProtocol oprot) throws org.apache.thrift.TException {
      struct.write(oprot);
    }
  };

  public static Tags decode(TProtocol _iprot) throws org.apache.thrift.TException {
    return CODEC.decode(_iprot);
  }

  public static void encode(Tags struct, TProtocol oprot) throws org.apache.thrift.TException {
    CODEC.encode(struct, oprot);
  }

  public Tags(
    Option<TagParse> model, 
    Result result, 
    Option<List<TagParse>> otherResult, 
    Option<String> errorMsg
  ) {
    this.model = model;
    this.result = result;
    this.otherResult = otherResult;
    this.errorMsg = errorMsg;
  }

  public Tags(
    Result result
  ) {
    this.model = Option.none();
    this.result = result;
    this.otherResult = Option.none();
    this.errorMsg = Option.none();
  }

  public TagParse getModel() {
    return this.model.get();
  }
  public boolean isSetModel() {
    return this.model.isDefined();
  }
  public Result getResult() {
    return this.result;
  }
  public boolean isSetResult() {
    return this.result != null;
  }
  public List<TagParse> getOtherResult() {
    return this.otherResult.get();
  }
  public boolean isSetOtherResult() {
    return this.otherResult.isDefined();
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
    if (model.isDefined()) {  _oprot.writeFieldBegin(ModelField);
      TagParse model_item = model.get();
      model_item.write(_oprot);
      _oprot.writeFieldEnd();
    }
      _oprot.writeFieldBegin(ResultField);
      Result result_item = result;
      _oprot.writeI32(result_item.getValue());
      _oprot.writeFieldEnd();
    if (otherResult.isDefined()) {  _oprot.writeFieldBegin(OtherResultField);
      List<TagParse> otherResult_item = otherResult.get();
      _oprot.writeListBegin(new TList(TType.STRUCT, otherResult_item.size()));
      for (TagParse otherResult_item_element : otherResult_item) {
        otherResult_item_element.write(_oprot);
      }
      _oprot.writeListEnd();
      _oprot.writeFieldEnd();
    }
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
    if (!(other instanceof Tags)) return false;
    Tags that = (Tags) other;
    return
this.model.equals(that.model) &&
this.result.equals(that.result) &&
this.otherResult.equals(that.otherResult) &&
this.errorMsg.equals(that.errorMsg);
  }

  @Override
  public String toString() {
    return "Tags(" + this.model + "," + this.result + "," + this.otherResult + "," + this.errorMsg + ")";
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * (this.model.isDefined() ? 0 : this.model.get().hashCode());
    hash = hash * (this.result == null ? 0 : this.result.hashCode());
    hash = hash * (this.otherResult.isDefined() ? 0 : this.otherResult.get().hashCode());
    hash = hash * (this.errorMsg.isDefined() ? 0 : this.errorMsg.get().hashCode());
    return hash;
  }
}