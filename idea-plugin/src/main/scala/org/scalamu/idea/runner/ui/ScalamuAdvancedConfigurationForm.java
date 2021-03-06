package org.scalamu.idea.runner.ui;

import com.intellij.AbstractBundle;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.scalamu.idea.ScalamuBundle$;
import org.scalamu.idea.runner.ScalamuDefaultSettings;
import org.scalamu.idea.runner.ScalamuRunConfiguration;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ScalamuAdvancedConfigurationForm {
  private EnvironmentVariablesComponent envVariables;
  private JFormattedTextField timeoutFactor;
  private JFormattedTextField timeoutConst;
  private JCheckBox enableVerboseLoggingCheckBox;
  private RawCommandLineEditor scalacOptions;
  private JPanel mainPanel;
  private JCheckBox aggregateDependencyModulesCheckBox;
  private ScalamuFilterTextFieldWithBrowseButton ignoredSymbols;
  private RawCommandLineEditor scalamuRunnerVmParams;
  private final AbstractBundle bundle = ScalamuBundle$.MODULE$;


  public ScalamuAdvancedConfigurationForm() {
    $$$setupUI$$$();
    setupTimeouts();
    setupIgnored();
  }

  public void apply(ScalamuRunConfiguration configuration) {
    envVariables.setEnvs(configuration.envVariables());
    timeoutConst.setText(Long.toString(configuration.timeoutConst()));
    timeoutFactor.setText(Double.toString(configuration.timeoutFactor()));
    enableVerboseLoggingCheckBox.setSelected(configuration.verboseLogging());
    aggregateDependencyModulesCheckBox.setSelected(configuration.aggregate());
    scalacOptions.setText(configuration.scalacParameters());
    ignoredSymbols.setData(configuration.getIgnoredSymbolsAsJava());
    scalamuRunnerVmParams.setText(configuration.scalamuRunnerVmParams());
  }

  private void setupIgnored() {
    ignoredSymbols.setData(ScalamuDefaultSettings.getIgnoredSymbolsAsJava());
  }

  private void setupTimeouts() {
    timeoutConst.setText(Long.toString(ScalamuDefaultSettings.timeoutConst()));
    timeoutConst.setInputVerifier(new InputVerifier() {
      @Override
      public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        String text = tf.getText();
        boolean isNumber = NumberUtils.isParsable(text);
        return isNumber && Long.parseLong(text) >= 0;
      }

      @Override
      public boolean shouldYieldFocus(JComponent input) {
        boolean isValid = verify(input);
        if (!isValid) {
          Messages.showErrorDialog(
                  bundle.getMessage("run.configuration.dialog.timeout.const.invalid"),
                  bundle.getMessage("run.configuration.dialog.invalid.title")
          );
        }
        return isValid;
      }
    });

    timeoutFactor.setText(Double.toString(ScalamuDefaultSettings.timeoutFactor()));
    timeoutFactor.setInputVerifier(new InputVerifier() {
      @Override
      public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        String text = tf.getText();
        boolean isNumber = NumberUtils.isParsable(text);
        return isNumber && Double.parseDouble(text) >= 0;
      }

      @Override
      public boolean shouldYieldFocus(JComponent input) {
        boolean isValid = verify(input);
        if (!isValid) {
          Messages.showErrorDialog(
                  bundle.getMessage("run.configuration.dialog.timeout.factor.invalid"),
                  bundle.getMessage("run.configuration.dialog.invalid.title")
          );
        }
        return isValid;
      }
    });
  }

  public String getTimeoutConstText() {
    return timeoutConst.getText();
  }

  public String getTimeoutFactorText() {
    return timeoutFactor.getText();
  }

  public boolean getVerboseLogging() {
    return enableVerboseLoggingCheckBox.isSelected();
  }

  public boolean getAggregate() {
    return aggregateDependencyModulesCheckBox.isSelected();
  }

  public String getScalamuJarRunnerVmParams() {
    return scalamuRunnerVmParams.getText();
  }

  public String getScalacParameters() {
    return scalacOptions.getText();
  }

  public Map<String, String> getEnvVariables() {
    return envVariables.getEnvs();
  }

  public String getIgnoredSymbols() {
    return ignoredSymbols.getText();
  }

  public JComponent getMainPanel() {
    return mainPanel;
  }

  private void createUIComponents() {
    ignoredSymbols = new ScalamuFilterTextFieldWithBrowseButton(
            bundle.getMessage("run.configuration.target.ignored.symbols.title"),
            bundle.getMessage("run.configuration.target.ignored.symbols.empty")
    );
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    createUIComponents();
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridLayoutManager(9, 4, new Insets(0, 0, 0, 0), -1, -1));
    final JLabel label1 = new JLabel();
    label1.setText("scalac options:");
    mainPanel.add(label1, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    scalacOptions = new RawCommandLineEditor();
    mainPanel.add(scalacOptions, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    envVariables = new EnvironmentVariablesComponent();
    mainPanel.add(envVariables, new GridConstraints(4, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Timeout factor:");
    mainPanel.add(label2, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    timeoutFactor = new JFormattedTextField();
    mainPanel.add(timeoutFactor, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    enableVerboseLoggingCheckBox = new JCheckBox();
    enableVerboseLoggingCheckBox.setText("Enable verbose logging");
    mainPanel.add(enableVerboseLoggingCheckBox, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setText("Timeout const, ms:");
    mainPanel.add(label3, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    timeoutConst = new JFormattedTextField();
    mainPanel.add(timeoutConst, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    aggregateDependencyModulesCheckBox = new JCheckBox();
    aggregateDependencyModulesCheckBox.setSelected(true);
    aggregateDependencyModulesCheckBox.setText("Aggregate dependency modules");
    mainPanel.add(aggregateDependencyModulesCheckBox, new GridConstraints(8, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    mainPanel.add(ignoredSymbols, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label4 = new JLabel();
    label4.setText("Ignored symbols:");
    mainPanel.add(label4, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label5 = new JLabel();
    label5.setText("Scalamu runner VM parameters:");
    mainPanel.add(label5, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    scalamuRunnerVmParams = new RawCommandLineEditor();
    scalamuRunnerVmParams.setText("-Xms512m -Xmx1500m");
    mainPanel.add(scalamuRunnerVmParams, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return mainPanel;
  }
}
