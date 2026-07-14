{{- /*
Common helpers for km-platform Helm chart.
*/}}

{{- /*
km-platform.common.names.suffix — derive a consistent name suffix.
*/}}
{{- define "km-platform.common.names.suffix" -}}
{{- $suffix := "" -}}
{{- if .Values.nameSuffix -}}
{{- $suffix = printf "-%s" .Values.nameSuffix -}}
{{- end -}}
{{- $suffix -}}
{{- end -}}

{{/*
km-platform.common.names.fullname — full name combining release name + component.
*/}}
{{- define "km-platform.common.names.fullname" -}}
{{- $name := default "km-platform" .Values.nameOverride -}}
{{- printf "%s-%s%s" $name .Chart.Name (include "km-platform.common.names.suffix" .) -}}
{{- end -}}

{{/*
km-platform.common.tplvalues — render the supplied values merged with defaults.
*/}}
{{- define "km-platform.common.tplvalues" -}}
{{- $values := deepCopy .Values -}}
{{- if not (hasKey $values "image") -}}
{{- $_ := set $values "image" (dict "repository" .Values.image.repository "tag" .Values.image.tag "pullPolicy" .Values.image.pullPolicy) -}}
{{- end -}}
{{- $values | toYaml -}}
{{- end -}}
