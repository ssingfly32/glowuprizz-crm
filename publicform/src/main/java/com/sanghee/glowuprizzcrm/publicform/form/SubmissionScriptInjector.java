package com.sanghee.glowuprizzcrm.publicform.form;

import org.springframework.stereotype.Component;

// 운영자가 등록한 HTML(AI가 생성, 우리 백엔드를 전혀 모름)을 그대로 서빙하되, 폼 제출을
// 가로채 우리 제출 API로 보내는 스크립트를 주입한다. Spring 없이 문자열만 다루는 순수
// 클래스라 빠르게 단위 테스트할 수 있다 (docs/adr/0007 참고).
@Component
public class SubmissionScriptInjector {

    private static final String CLOSING_BODY_TAG = "</body>";

    public String inject(String html, String submitUrl) {
        String script = buildScript(submitUrl);
        int index = html.lastIndexOf(CLOSING_BODY_TAG);
        if (index == -1) {
            return html + script;
        }
        return html.substring(0, index) + script + html.substring(index);
    }

    private String buildScript(String submitUrl) {
        return """
                <script>
                (function () {
                  var form = document.querySelector('form');
                  if (!form) { return; }
                  form.addEventListener('submit', function (event) {
                    event.preventDefault();
                    var formData = new FormData(form);
                    var data = {};
                    formData.forEach(function (value, key) { data[key] = value; });
                    fetch('%s', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      credentials: 'include',
                      body: JSON.stringify(data)
                    });
                  });
                })();
                </script>
                """.formatted(submitUrl);
    }
}
