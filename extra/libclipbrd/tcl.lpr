library tcl;

{$mode objfpc}{$H+}

uses
  Classes,
  Clipbrd,
  LCLIntf,
  Windows,
  Interfaces,
  stringutils;

var
  HiddenApp: TComponent;

  procedure EnsureLCL;
  begin
    if HiddenApp = nil then
    begin
      HiddenApp := TComponent.Create(nil);
    end;
  end;

  procedure Popup(const title, Text: string);
  begin
    MessageBoxA(0, PChar(Text), PChar(title), MB_OK or MB_ICONINFORMATION or MB_TOPMOST);
  end;

  function SetClipboardUTF8(AText: pchar): integer; stdcall;
  begin
    try
      EnsureLCL;
      Clipboard.AsText := GBKToUTF8(AText);
      Popup('Tip', 'Copied to Clipboard');
      Result := 0;
    except
      Result := -1;
    end;
  end;

exports
  SetClipboardUTF8 Name 'SetClipboardUTF8';

begin
  HiddenApp := nil;
end.
