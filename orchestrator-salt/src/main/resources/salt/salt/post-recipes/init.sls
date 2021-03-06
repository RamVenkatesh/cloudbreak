{% for hg, args in pillar.get('recipes', {}).items() %}
{% if grains['hostgroup'] == hg %}
{% for scrip_name in args['post'] %}
/opt/scripts/post/{{ scrip_name }}:
  file.managed:
     - source:
       - salt://post-recipes/scripts/{{ scrip_name }}
       - salt://post-recipes/scripts/post-date.sh
     - makedirs: True
     - mode: 755

run_post_script_{{ scrip_name }}:
  cmd.run:
    - name: /opt/scripts/post/{{ scrip_name }}
    - onlyif:
      - ls /opt/scripts/post/{{ scrip_name }}
{% endfor %}
{% endif %}
{% endfor %}