package fr.perso.nfelix.app.ui.services.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

/**
 * ReportGeneration Utils
 *
 * @author N.FELIX
 */
@Slf4j
public abstract class ReportGenerationUtils {

  private static final String ENCODING            = "UTF-8";
  private static final String FTL_EXTENSION       = ".ftl";
  private static final String THYMELEAF_EXTENSION = ".thy";
  private static final String HTML_EXTENSION      = ".html";

  /**
   * generate data according to Thymeleaf engine and template...
   *
   * @param thymeleafTemplate template file
   * @param data              date to merge into
   * @return generated file path
   * @throws Exception in case of...
   */
  public static String generateThymeleafReport(final File thymeleafTemplate, Map<String, Object> data)
      throws Exception {
    LOGGER.debug(">>> generateFreemarkerReport({})", thymeleafTemplate);

    TemplateEngine engine = new TemplateEngine();
    final FileTemplateResolver templateResolver = new FileTemplateResolver();
    templateResolver.setSuffix(THYMELEAF_EXTENSION);
    templateResolver.setTemplateMode(TemplateMode.HTML);

    final String templatePath = thymeleafTemplate.getAbsolutePath();
    templateResolver.setPrefix(FilenameUtils.normalizeNoEndSeparator(FilenameUtils.getFullPath(templatePath)) + File.separatorChar);
    templateResolver.setCharacterEncoding(ENCODING);
    engine.setTemplateResolver(templateResolver);

    // dumpData(data);

    final Context ctx = new Context();
    ctx.setVariables(data);

    String resultFilePath = StringUtils.removeEnd(thymeleafTemplate.getAbsolutePath(), THYMELEAF_EXTENSION);
    resultFilePath += HTML_EXTENSION;

    // Merge data-model with template
    try(FileOutputStream fos = new FileOutputStream(new File(resultFilePath)); Writer fStream = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
      engine.process(FilenameUtils.getBaseName(templatePath), ctx, fStream);
    }
    finally {
      LOGGER.debug("<<< generateFreemarkerReport({})", resultFilePath);
    }
    return resultFilePath;
  }

  private static void dumpData(Map<String, Object> data) {
    for(Map.Entry<String, Object> entry : data.entrySet()) {
      LOGGER.info(entry.getKey());
      final Object value = entry.getValue();
      if(value == null) {
        LOGGER.info("  no value");
      }
      else if(value instanceof List) {
        List<String> item = (List<String>) value;
        for(int i = 0; i < item.size(); i++) {
          LOGGER.info("  {} = {}", i, item.get(i));
        }
      }
      else {
        LOGGER.info("  not an expected class:'{}'", value.getClass().getSimpleName());
      }
    }
  }
}
