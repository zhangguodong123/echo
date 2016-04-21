package cn.com.cig.adsense.vo.fix;

import com.twitter.scrooge.ThriftStruct;
import com.twitter.scrooge.ThriftStructCodec;
import com.twitter.scrooge.ThriftStructCodec3;
import org.apache.thrift.protocol.*;

@javax.annotation.Generated(value = "com.twitter.scrooge.Compiler", date = "2014-12-06T02:04:33.859+0800")
public class TagParse implements ThriftStruct {
  private static final TStruct STRUCT = new TStruct("TagParse");
  private static final TField TagIdField = new TField("tagId", TType.I32, (short) 1);
  final int tagId;
  private static final TField ScoreField = new TField("score", TType.DOUBLE, (short) 2);
  final double score;

  public static class Builder {
    private int _tagId = 0;

    public Builder tagId(int value) {
      this._tagId = value;
      return this;
    }

    public Builder unsetTagId() {
      this._tagId = 0;
      return this;
    }
    private double _score = 0.0;

    public Builder score(double value) {
      this._score = value;
      return this;
    }

    public Builder unsetScore() {
      this._score = 0.0;
      return this;
    }

    public TagParse build() {
      return new TagParse(
        this._tagId,
        this._score    );
    }
  }

  public Builder copy() {
    Builder builder = new Builder();
    builder.tagId(this.tagId);
    builder.score(this.score);
    return builder;
  }

  public static ThriftStructCodec<TagParse> CODEC = new ThriftStructCodec3<TagParse>() {
    @Override
    public TagParse decode(TProtocol _iprot) throws org.apache.thrift.TException {
      Builder builder = new Builder();
      int tagId = 0;
      double score = 0.0;
      Boolean _done = false;
      _iprot.readStructBegin();
      while (!_done) {
        TField _field = _iprot.readFieldBegin();
        if (_field.type == TType.STOP) {
          _done = true;
        } else {
          switch (_field.id) {
            case 1: /* tagId */
              switch (_field.type) {
                case TType.I32:
                  Integer tagId_item;
                  tagId_item = _iprot.readI32();
                  tagId = tagId_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.tagId(tagId);
              break;
            case 2: /* score */
              switch (_field.type) {
                case TType.DOUBLE:
                  Double score_item;
                  score_item = _iprot.readDouble();
                  score = score_item;
                  break;
                default:
                  TProtocolUtil.skip(_iprot, _field.type);
              }
              builder.score(score);
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
    public void encode(TagParse struct, TProtocol oprot) throws org.apache.thrift.TException {
      struct.write(oprot);
    }
  };

  public static TagParse decode(TProtocol _iprot) throws org.apache.thrift.TException {
    return CODEC.decode(_iprot);
  }

  public static void encode(TagParse struct, TProtocol oprot) throws org.apache.thrift.TException {
    CODEC.encode(struct, oprot);
  }

  public TagParse(
    int tagId, 
    double score
  ) {
    this.tagId = tagId;
    this.score = score;
  }


  public int getTagId() {
    return this.tagId;
  }
  public double getScore() {
    return this.score;
  }

  public void write(TProtocol _oprot) throws org.apache.thrift.TException {
    validate();
    _oprot.writeStructBegin(STRUCT);
      _oprot.writeFieldBegin(TagIdField);
      Integer tagId_item = tagId;
      _oprot.writeI32(tagId_item);
      _oprot.writeFieldEnd();
      _oprot.writeFieldBegin(ScoreField);
      Double score_item = score;
      _oprot.writeDouble(score_item);
      _oprot.writeFieldEnd();
    _oprot.writeFieldStop();
    _oprot.writeStructEnd();
  }

  private void validate() throws org.apache.thrift.protocol.TProtocolException {
  }


  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TagParse)) return false;
    TagParse that = (TagParse) other;
    return
      this.tagId == that.tagId &&

      this.score == that.score
;
  }

  @Override
  public String toString() {
    return "TagParse(" + this.tagId + "," + this.score + ")";
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * new Integer(this.tagId).hashCode();
    hash = hash * new Double(this.score).hashCode();
    return hash;
  }
}