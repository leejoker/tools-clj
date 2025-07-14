package scoop;

public class ScoopTemplate {
  private static final String SCOOP_PS1_TEMPLATE = """
      # %s
      $path = Join-Path $PSScriptRoot "..\\plugins\\scoop\\apps\\scoop\\current\\bin\\scoop.ps1"
      if ($MyInvocation.ExpectingInput) { $input | & $path  @args } else { & $path  @args }
      exit $LASTEXITCODE
      """;

  public static final String SCOOP_CMD_TEMPLATE = """
      @rem %s
      @echo off
      where /q pwsh.exe
      if %%errorlevel%% equ 0 (
          pwsh -noprofile -ex unrestricted -file \"%s\"  %%*
      ) else (
          powershell -noprofile -ex unrestricted -file \"%s\"  %%*
      )
      """;

  public static final String SCOOP_SH_TEMPLATE = """
      #!/bin/sh
      # %s
      if command -v pwsh.exe > /dev/null 2>&1; then
          pwsh.exe -noprofile -ex unrestricted -file "%s"  "$@"
      else
          powershell.exe -noprofile -ex unrestricted -file "%s"  "$@"
      fi
      """;

  public static String scoopPowershell(String path) {
    return String.format(SCOOP_PS1_TEMPLATE, path);
  }

  public static String scoopCmd(String path) {
    return String.format(SCOOP_CMD_TEMPLATE, path, path, path);
  }

  public static String scoopShell(String path) {
    return String.format(SCOOP_SH_TEMPLATE, path, path, path);
  }
}
