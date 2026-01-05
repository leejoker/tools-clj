library tcl;

{$mode objfpc}{$H+}

uses
  {$IFDEF MSWINDOWS}
  Windows,
  Clipbrd,
  {$ENDIF}
  {$IFDEF UNIX}
  {$IFDEF LINUX}
  cthreads,
  Dialogs,
  process,
  {$ENDIF}
  {$IFDEF DARWIN}
  Dialogs,
  Clipbrd,
  {$ENDIF}
  {$ENDIF}
  SysUtils,
  LCLIntf,
  Interfaces,
  Classes,
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
    {$IFDEF MSWINDOWS}
    MessageBoxA(0, PChar(Text), PChar(title), MB_OK or MB_ICONINFORMATION or MB_TOPMOST);
    {$ENDIF}
    {$IFDEF UNIX}
    MessageDlg(PChar(title), PChar(Text), mtInformation, [mbOK], 0);
    {$ENDIF}
  end;

  function ShellQuote(const S: string): string;
  begin
    Result := '''' + StringReplace(S, '''', '''''', [rfReplaceAll]) + '''';
  end;

  function SetClipboardUTF8(AText: pchar): integer; stdcall;
  begin
    try
      EnsureLCL;
      {$IFDEF MSWINDOWS}
      Clipboard.AsText := GBKToUTF8(AText);
      {$ENDIF}
      {$IFDEF UNIX}
      {$IFDEF DARWIN}
      Clipboard.AsText := AText;
      {$ENDIF}
      {$IFDEF LINUX}
        with TProcess.Create(nil) do
        try
          Executable := 'bash';
          Parameters.Add('-c');
          Parameters.Add('echo -n ' + ShellQuote(AText) + ' | xclip -selection clipboard');
          Options := [poUsePipes, poWaitOnExit];
          Execute;
        except
            on E: Exception do
            begin
              Writeln('Catch Error: ', E.Message);
              Free;
            end;
        end;
      {$ENDIF}
      {$ENDIF}
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
