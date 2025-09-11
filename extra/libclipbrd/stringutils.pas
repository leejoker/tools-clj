unit stringutils;

{$mode ObjFPC}{$H+}

interface

uses
  Classes, SysUtils, LazUTF8, Windows;

type
  TCharSet = (csASCII, csUTF8, csUTF16LE, csUTF16BE, csUnknown);

function QuickGuess(P: pchar; Len: integer): TCharSet;
function IsValidUTF8(P: pchar; ByteLen: integer): boolean;
function GB18030Score(P: pchar; Len: integer): integer;
function GuessEncoding(P: pchar; ByteLen: integer): string;
function GBKToUTF8(const s: rawbytestring): utf8string;

implementation

function QuickGuess(P: pchar; Len: integer): TCharSet;
begin
  if Len < 2 then Exit(csASCII);
  // UTF-16 BOM
  if (PWord(P)^ = $FEFF) then Exit(csUTF16LE);
  if (PWord(P)^ = $FFFE) then Exit(csUTF16BE);
  // UTF-8 BOM
  if (Len >= 3) and (pbyte(P)^ = $EF) and (pbyte(P + 1)^ = $BB) and (pbyte(P + 2)^ = $BF) then
    Exit(csUTF8);
  // 纯 ASCII？
  Result := csASCII;
  for Len := 0 to Len - 1 do
    if Ord((P + Len)^) > $7F then
    begin
      Result := csUnknown; // 可能是 UTF-8/ANSI/GBK…
      Break;
    end;
end;

// 返回 True = 合法 UTF-8
function IsValidUTF8(P: pchar; ByteLen: integer): boolean;
var
  i, c, n, j: integer;
begin
  i := 0;
  while i < ByteLen do
  begin
    c := byte((P + i)^);
    if c <= $7F then
      Inc(i)
    else if c and $E0 = $C0 then
    begin
      n := 1;
      c := c and $1F;
    end
    else if c and $F0 = $E0 then
    begin
      n := 2;
      c := c and $0F;
    end
    else if c and $F8 = $F0 then
    begin
      n := 3;
      c := c and $07;
    end
    else
      Exit(False);
    for j := 1 to n do
    begin
      Inc(i);
      if i >= ByteLen then Exit(False);
      if byte((P + i)^) and $C0 <> $80 then Exit(False);
      c := (c shl 6) or (byte((P + i)^) and $3F);
    end;
    if (c < $80) or (c > $10FFFF) then Exit(False);
  end;
  Result := True;
end;

function GB18030Score(P: pchar; Len: integer): integer;
var
  i: integer;
  b1, b2: byte;
begin
  Result := 0;
  i := 0;
  while i < Len - 1 do
  begin
    b1 := byte((P + i)^);
    if (b1 >= $81) and (b1 <= $FE) then
    begin
      b2 := byte((P + i + 1)^);
      if not (((b2 >= $40) and (b2 <= $7E)) or ((b2 >= $80) and (b2 <= $FE))) then
        Inc(Result, 100); // 高额罚分
      Inc(i, 2);
    end
    else
      Inc(i);
  end;
end;

function GuessEncoding(P: pchar; ByteLen: integer): string;
begin
  if QuickGuess(P, ByteLen) = csUTF16LE then Exit('UTF-16LE');
  if QuickGuess(P, ByteLen) = csUTF16BE then Exit('UTF-16BE');
  if IsValidUTF8(P, ByteLen) then Exit('UTF-8');
  if GB18030Score(P, ByteLen) < 10 then Exit('GB18030');
  Exit('ISO-8859-1'); // 兜底
end;

function GBKToUTF8(const s: rawbytestring): utf8string;
var
  wlen, u8len: integer;
  ws: unicodestring;
begin
  wlen := MultiByteToWideChar(936, 0, PChar(s), Length(s), nil, 0);
  SetLength(ws, wlen);
  MultiByteToWideChar(936, 0, PChar(s), Length(s), pwidechar(ws), wlen);
  u8len := WideCharToMultiByte(CP_UTF8, 0, pwidechar(ws), wlen, nil, 0, nil, nil);
  SetLength(Result, u8len);
  WideCharToMultiByte(CP_UTF8, 0, pwidechar(ws), wlen, PChar(Result), u8len, nil, nil);
end;

end.
